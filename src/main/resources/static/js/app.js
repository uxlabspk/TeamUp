// General application utilities

// Theme management
function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);
}

// Call on page load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTheme);
} else {
    initTheme();
}

// Utility function to format timestamps
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    // Less than a minute
    if (diff < 60000) {
        return 'Just now';
    }

    // Less than an hour
    if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000);
        return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    }

    // Less than a day
    if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    }

    // Otherwise show date and time
    return date.toLocaleString();
}

// Utility function to get authorization header
function getAuthHeader() {
    const token = localStorage.getItem('token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
}

// Utility function to handle API errors
function handleApiError(error, fallbackMessage = 'An error occurred') {
    console.error('API Error:', error);
    if (error.status === 401) {
        // Unauthorized - redirect to login
        localStorage.clear();
        window.location.href = '/login';
    } else if (error.status === 403) {
        alert('You do not have permission to perform this action');
    } else {
        alert(fallbackMessage);
    }
}

// Utility function to escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Export for use in other scripts
window.TeamUpUtils = {
    formatTimestamp,
    getAuthHeader,
    handleApiError,
    escapeHtml
};

