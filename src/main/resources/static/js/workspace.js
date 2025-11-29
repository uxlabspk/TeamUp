// Workspace and Chat functionality
let stompClient = null;
let currentChannelId = null;
let currentWorkspaceId = null;

document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    const username = localStorage.getItem('username');

    if (!token || !userId) {
        window.location.href = '/login';
        return;
    }

    // Initialize UI
    document.getElementById('user-name').textContent = username;
    loadWorkspaces(userId);
    connectWebSocket();
    setupEventListeners();
});

function setupEventListeners() {
    // Theme toggle
    document.getElementById('theme-toggle').addEventListener('click', toggleTheme);

    // Logout
    document.getElementById('logout-btn').addEventListener('click', logout);

    // Message form
    document.getElementById('message-form').addEventListener('submit', sendMessage);

    // File attachment
    document.getElementById('attach-btn').addEventListener('click', () => {
        document.getElementById('file-input').click();
    });

    // Create workspace
    document.getElementById('create-workspace-btn').addEventListener('click', createWorkspace);

    // Start meeting
    document.getElementById('start-meeting-btn').addEventListener('click', startMeeting);
}

async function loadWorkspaces(userId) {
    const token = localStorage.getItem('token');

    console.log('Loading workspaces for user:', userId);

    try {
        const response = await fetch(`/api/workspaces/user/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        console.log('Workspaces response:', response.status);

        if (response.ok) {
            const workspaces = await response.json();
            console.log('Workspaces loaded:', workspaces);
            displayWorkspaces(workspaces);
        } else {
            console.error('Failed to load workspaces:', response.status);
            const container = document.getElementById('workspaces-container');
            container.innerHTML = '<p style="color: red; padding: 10px;">Failed to load workspaces</p>';
        }
    } catch (error) {
        console.error('Error loading workspaces:', error);
        const container = document.getElementById('workspaces-container');
        container.innerHTML = '<p style="color: red; padding: 10px;">Error: ' + error.message + '</p>';
    }
}

function displayWorkspaces(workspaces) {
    const container = document.getElementById('workspaces-container');
    container.innerHTML = '';

    if (!workspaces || workspaces.length === 0) {
        container.innerHTML = '<p style="color: var(--text-secondary); padding: 10px; font-size: 0.9rem;">No workspaces yet. Create one!</p>';
        return;
    }

    workspaces.forEach(workspace => {
        const div = document.createElement('div');
        div.className = 'workspace-item';
        div.textContent = workspace.name;
        div.onclick = () => selectWorkspace(workspace.id);
        container.appendChild(div);
    });
}

async function selectWorkspace(workspaceId) {
    currentWorkspaceId = workspaceId;
    const token = localStorage.getItem('token');

    try {
        const response = await fetch(`/api/workspaces/${workspaceId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const workspace = await response.json();
            loadChannels(workspace.channels);
        }
    } catch (error) {
        console.error('Error loading workspace:', error);
    }
}

function loadChannels(channels) {
    const container = document.getElementById('channels-container');
    container.innerHTML = '<h3>Channels</h3>';

    channels.forEach(channel => {
        const div = document.createElement('div');
        div.className = 'channel-item';
        div.textContent = '# ' + channel.name;
        div.onclick = () => selectChannel(channel.id);
        container.appendChild(div);
    });
}

async function selectChannel(channelId) {
    currentChannelId = channelId;

    // Subscribe to channel messages
    if (stompClient && stompClient.connected) {
        stompClient.subscribe(`/topic/channel/${channelId}`, onMessageReceived);
    }

    // Load message history
    await loadMessages(channelId);
}

async function loadMessages(channelId) {
    const token = localStorage.getItem('token');

    try {
        const response = await fetch(`/api/messages/channel/${channelId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const messages = await response.json();
            displayMessages(messages);
        }
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

function displayMessages(messages) {
    const container = document.getElementById('messages-container');
    container.innerHTML = '';

    messages.reverse().forEach(message => {
        appendMessage(message);
    });
}

function appendMessage(message) {
    const container = document.getElementById('messages-container');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';

    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.textContent = message.senderUsername.charAt(0).toUpperCase();

    const content = document.createElement('div');
    content.className = 'message-content';

    const header = document.createElement('div');
    header.className = 'message-header';

    const author = document.createElement('span');
    author.className = 'message-author';
    author.textContent = message.senderUsername;

    const time = document.createElement('span');
    time.className = 'message-time';
    time.textContent = new Date(message.timestamp).toLocaleTimeString();

    header.appendChild(author);
    header.appendChild(time);

    const text = document.createElement('div');
    text.className = 'message-text';
    text.textContent = message.content;

    content.appendChild(header);
    content.appendChild(text);

    messageDiv.appendChild(avatar);
    messageDiv.appendChild(content);

    container.appendChild(messageDiv);
    container.scrollTop = container.scrollHeight;
}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    console.log('WebSocket connected');
}

function onError(error) {
    console.error('WebSocket error:', error);
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    appendMessage(message);
}

function sendMessage(e) {
    e.preventDefault();

    if (!currentChannelId) {
        alert('Please select a channel first');
        return;
    }

    const messageInput = document.getElementById('message-input');
    const content = messageInput.value.trim();

    if (content && stompClient) {
        const messageData = {
            channelId: currentChannelId,
            senderId: localStorage.getItem('userId'),
            content: content
        };

        stompClient.send(`/app/chat/${currentChannelId}`, {}, JSON.stringify(messageData));
        messageInput.value = '';
    }
}

async function createWorkspace() {
    const name = prompt('Enter workspace name:');
    if (!name) return;

    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    console.log('Creating workspace:', { name, userId });

    try {
        const response = await fetch(`/api/workspaces?userId=${userId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, description: '' })
        });

        console.log('Workspace creation response:', response.status);

        if (response.ok) {
            const workspace = await response.json();
            console.log('Workspace created successfully:', workspace);
            alert('Workspace created: ' + workspace.name);
            loadWorkspaces(userId);
        } else {
            const error = await response.text();
            console.error('Failed to create workspace:', response.status, error);
            alert('Failed to create workspace: ' + error);
        }
    } catch (error) {
        console.error('Error creating workspace:', error);
        alert('Error creating workspace: ' + error.message);
    }
}

async function startMeeting() {
    if (!currentChannelId) {
        alert('Please select a channel first');
        return;
    }

    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    try {
        const response = await fetch(`/api/meetings/channel/${currentChannelId}?userId=${userId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const meeting = await response.json();
            window.open(`/meeting/${meeting.id}`, '_blank');
        }
    } catch (error) {
        console.error('Error starting meeting:', error);
    }
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
}

function logout() {
    localStorage.clear();
    window.location.href = '/login';
}

