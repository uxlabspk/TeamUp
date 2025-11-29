// LiveKit meeting functionality
let room = null;
let localVideo = null;
let localAudio = null;

document.addEventListener('DOMContentLoaded', async function() {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (!token || !userId || !meetingId) {
        alert('Invalid meeting parameters');
        window.close();
        return;
    }

    await initializeMeeting(meetingId, userId);
    setupControls();
});

async function initializeMeeting(meetingId, userId) {
    try {
        // Get meeting token from backend
        const response = await fetch(`/api/meetings/${meetingId}/token?userId=${userId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to get meeting token');
        }

        const data = await response.json();
        const livekitToken = data.token;

        // Connect to LiveKit room
        room = new LivekitClient.Room();

        room.on(LivekitClient.RoomEvent.TrackSubscribed, handleTrackSubscribed);
        room.on(LivekitClient.RoomEvent.TrackUnsubscribed, handleTrackUnsubscribed);
        room.on(LivekitClient.RoomEvent.ParticipantConnected, handleParticipantConnected);
        room.on(LivekitClient.RoomEvent.ParticipantDisconnected, handleParticipantDisconnected);

        await room.connect('ws://localhost:7880', livekitToken);

        // Enable local camera and microphone
        await room.localParticipant.setCameraEnabled(true);
        await room.localParticipant.setMicrophoneEnabled(true);

        // Display local video
        const localVideoTrack = room.localParticipant.videoTracks.values().next().value?.track;
        if (localVideoTrack) {
            attachVideoTrack(localVideoTrack, 'local', true);
        }

    } catch (error) {
        console.error('Error initializing meeting:', error);
        alert('Failed to join meeting');
    }
}

function setupControls() {
    document.getElementById('toggle-video').addEventListener('click', toggleVideo);
    document.getElementById('toggle-audio').addEventListener('click', toggleAudio);
    document.getElementById('share-screen').addEventListener('click', shareScreen);
    document.getElementById('leave-meeting').addEventListener('click', leaveMeeting);
}

async function toggleVideo() {
    if (room && room.localParticipant) {
        const enabled = room.localParticipant.isCameraEnabled;
        await room.localParticipant.setCameraEnabled(!enabled);
        document.getElementById('toggle-video').textContent = enabled ? '📹' : '🚫📹';
    }
}

async function toggleAudio() {
    if (room && room.localParticipant) {
        const enabled = room.localParticipant.isMicrophoneEnabled;
        await room.localParticipant.setMicrophoneEnabled(!enabled);
        document.getElementById('toggle-audio').textContent = enabled ? '🎤' : '🚫🎤';
    }
}

async function shareScreen() {
    if (room && room.localParticipant) {
        try {
            await room.localParticipant.setScreenShareEnabled(true);
        } catch (error) {
            console.error('Error sharing screen:', error);
        }
    }
}

async function leaveMeeting() {
    if (room) {
        await room.disconnect();
    }
    window.close();
}

function handleTrackSubscribed(track, publication, participant) {
    if (track.kind === 'video') {
        attachVideoTrack(track, participant.identity, false);
    }
}

function handleTrackUnsubscribed(track, publication, participant) {
    if (track.kind === 'video') {
        removeVideoTrack(participant.identity);
    }
}

function handleParticipantConnected(participant) {
    console.log('Participant connected:', participant.identity);
    updateParticipantsList();
}

function handleParticipantDisconnected(participant) {
    console.log('Participant disconnected:', participant.identity);
    removeVideoTrack(participant.identity);
    updateParticipantsList();
}

function attachVideoTrack(track, participantId, isLocal) {
    const videoGrid = document.getElementById('video-grid');

    const participantDiv = document.createElement('div');
    participantDiv.className = 'video-participant';
    participantDiv.id = `participant-${participantId}`;

    const videoElement = track.attach();
    videoElement.style.width = '100%';
    videoElement.style.height = '100%';

    if (isLocal) {
        videoElement.style.transform = 'scaleX(-1)'; // Mirror local video
    }

    const nameLabel = document.createElement('div');
    nameLabel.style.position = 'absolute';
    nameLabel.style.bottom = '10px';
    nameLabel.style.left = '10px';
    nameLabel.style.background = 'rgba(0,0,0,0.7)';
    nameLabel.style.color = 'white';
    nameLabel.style.padding = '5px 10px';
    nameLabel.style.borderRadius = '4px';
    nameLabel.textContent = isLocal ? 'You' : participantId;

    participantDiv.appendChild(videoElement);
    participantDiv.appendChild(nameLabel);
    videoGrid.appendChild(participantDiv);
}

function removeVideoTrack(participantId) {
    const participantDiv = document.getElementById(`participant-${participantId}`);
    if (participantDiv) {
        participantDiv.remove();
    }
}

function updateParticipantsList() {
    if (!room) return;

    const participantsList = document.getElementById('participants-list');
    participantsList.innerHTML = '';

    // Add local participant
    const localDiv = document.createElement('div');
    localDiv.className = 'participant-item';
    localDiv.textContent = 'You (Host)';
    participantsList.appendChild(localDiv);

    // Add remote participants
    room.participants.forEach((participant) => {
        const div = document.createElement('div');
        div.className = 'participant-item';
        div.textContent = participant.identity;
        participantsList.appendChild(div);
    });
}

