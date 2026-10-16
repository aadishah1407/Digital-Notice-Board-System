const loginForm = document.getElementById('loginForm');
const noticeForm = document.getElementById('noticeForm');
const loginMessage = document.getElementById('loginMessage');
const noticeMessage = document.getElementById('noticeMessage');
const loginSection = document.getElementById('loginSection');
const noticeSection = document.getElementById('noticeSection');
const sessionMessage = document.getElementById('sessionMessage');
const logoutButton = document.getElementById('logoutButton');

let authState = {
  username: '',
  password: ''
};

function setMessage(element, text, isError = false) {
  element.textContent = text;
  element.className = `message ${isError ? 'error' : 'success'}`;
}

function buildBasicAuthHeader(username, password) {
  const raw = `${username}:${password}`;
  return 'Basic ' + btoa(raw);
}

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();

  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;

  if (!username || !password) {
    setMessage(loginMessage, 'Username and password are required.', true);
    return;
  }

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ username, password })
    });

    const data = await response.json();

    if (!response.ok || !data.authenticated) {
      setMessage(loginMessage, data.message || 'Login failed.', true);
      return;
    }

    authState.username = username;
    authState.password = password;
    sessionMessage.textContent = `Logged in as ${data.username}.`;
    loginSection.classList.add('hidden');
    noticeSection.classList.remove('hidden');
  } catch (error) {
    setMessage(loginMessage, 'Login failed. Please try again.', true);
  }
});

logoutButton.addEventListener('click', () => {
  authState.username = '';
  authState.password = '';
  noticeForm.reset();
  setMessage(noticeMessage, '', false);
  loginSection.classList.remove('hidden');
  noticeSection.classList.add('hidden');
});

noticeForm.addEventListener('submit', async (event) => {
  event.preventDefault();

  if (!authState.username || !authState.password) {
    setMessage(noticeMessage, 'Please log in before creating a notice.', true);
    return;
  }

  const payload = {
    title: document.getElementById('title').value.trim(),
    description: document.getElementById('description').value.trim(),
    department: document.getElementById('department').value.trim(),
    academicYear: document.getElementById('academicYear').value.trim(),
    publishedAt: `${document.getElementById('publishedAt').value}:00`,
    expiryDate: document.getElementById('expiryDate').value
  };

  try {
    const response = await fetch('/api/admin/notices', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': buildBasicAuthHeader(authState.username, authState.password)
      },
      body: JSON.stringify(payload)
    });

    const data = await response.json();

    if (!response.ok) {
      setMessage(noticeMessage, data.message || 'Notice creation failed.', true);
      return;
    }

    setMessage(noticeMessage, `Notice created successfully: ${data.title}`, false);
    noticeForm.reset();
  } catch (error) {
    setMessage(noticeMessage, 'Notice creation failed. Please try again.', true);
  }
});
