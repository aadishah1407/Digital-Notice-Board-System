const loginSection = document.getElementById('loginSection');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const dashboardSection = document.getElementById('dashboardSection');
const createSection = document.getElementById('createSection');
const noticeForm = document.getElementById('noticeForm');
const noticeList = document.getElementById('noticeList');
const loginMessage = document.getElementById('loginMessage');
const registerMessage = document.getElementById('registerMessage');
const noticeMessage = document.getElementById('noticeMessage');
const dashboardMessage = document.getElementById('dashboardMessage');
const authHeading = document.getElementById('authHeading');
const authCopy = document.getElementById('authCopy');
const authSwitch = document.getElementById('authSwitch');
const sessionMessage = document.getElementById('sessionMessage');
const submitNoticeButton = document.getElementById('submitNoticeButton');

let authState = { username: '', password: '' };

function setMessage(element, text, isError = false) {
  element.textContent = text;
  element.className = `message${text ? ` ${isError ? 'error' : 'success'}` : ''}`;
}

function authHeaders(json = false) {
  const headers = {};
  if (json) headers['Content-Type'] = 'application/json';
  if (authState.username && authState.password) {
    headers.Authorization = `Basic ${btoa(`${authState.username}:${authState.password}`)}`;
  }
  return headers;
}

async function responseMessage(response, fallback) {
  try {
    const body = await response.json();
    return body.message || body.detail || fallback;
  } catch {
    return fallback;
  }
}

function showLogin() {
  loginForm.classList.remove('hidden');
  registerForm.classList.add('hidden');
  authSwitch.classList.remove('hidden');
  authHeading.textContent = 'Welcome back';
  authCopy.textContent = 'Sign in to manage campus notices.';
  setMessage(loginMessage, '');
  setMessage(registerMessage, '');
}

function showRegister() {
  loginForm.classList.add('hidden');
  registerForm.classList.remove('hidden');
  authSwitch.classList.add('hidden');
  authHeading.textContent = 'Create your account';
  authCopy.textContent = 'Register to start managing campus announcements.';
  setMessage(loginMessage, '');
  setMessage(registerMessage, '');
}

function showDashboard() {
  loginSection.classList.add('hidden');
  createSection.classList.add('hidden');
  dashboardSection.classList.remove('hidden');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function formatDate(value, options = { dateStyle: 'medium' }) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat(undefined, options).format(date);
}

function renderNotices(notices) {
  noticeList.replaceChildren();
  if (!notices.length) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.innerHTML = '<div class="empty-icon" aria-hidden="true">✦</div><h3>No notices yet</h3><p>Create your first campus notice and it will appear here.</p>';
    noticeList.append(empty);
    return;
  }

  notices.forEach((notice) => {
    const article = document.createElement('article');
    article.className = 'notice-card';
    const title = document.createElement('h3');
    title.textContent = notice.title;
    const description = document.createElement('p');
    description.className = 'notice-description';
    description.textContent = notice.description;
    const department = document.createElement('span');
    department.className = 'notice-tag';
    department.textContent = notice.department;
    const meta = document.createElement('div');
    meta.className = 'notice-meta';
    meta.innerHTML = '<span>Academic year <strong></strong></span><span>Published <strong></strong></span><span>Expires <strong></strong></span>';
    const values = meta.querySelectorAll('strong');
    values[0].textContent = notice.academicYear || '—';
    values[1].textContent = formatDate(notice.publishedAt, { dateStyle: 'medium', timeStyle: 'short' });
    values[2].textContent = formatDate(notice.expiryDate);
    const heading = document.createElement('div');
    heading.className = 'notice-card-heading';
    heading.append(title, department);
    article.append(heading, description, meta);
    noticeList.append(article);
  });
}

async function loadNotices() {
  setMessage(dashboardMessage, 'Loading notices…');
  try {
    const response = await fetch('/api/admin/notices', { headers: authHeaders() });
    if (!response.ok) throw new Error(await responseMessage(response, 'Could not load notices.'));
    renderNotices(await response.json());
    setMessage(dashboardMessage, '');
  } catch (error) {
    setMessage(dashboardMessage, error.message || 'Could not load notices. Please try again.', true);
  }
}

document.getElementById('showRegister').addEventListener('click', showRegister);
document.getElementById('showLogin').addEventListener('click', showLogin);

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await response.json();
    if (!response.ok || !data.authenticated) {
      setMessage(loginMessage, data.message || 'Login failed. Check your username and password.', true);
      return;
    }
    authState = { username, password };
    sessionMessage.textContent = `Signed in as ${data.username}`;
    showDashboard();
    await loadNotices();
  } catch {
    setMessage(loginMessage, 'Login failed. Please try again.', true);
  }
});

registerForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const username = document.getElementById('registerUsername').value.trim();
  const password = document.getElementById('registerPassword').value;
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!response.ok) {
      setMessage(registerMessage, await responseMessage(response, 'Registration failed. Please try again.'), true);
      return;
    }
    registerForm.reset();
    showLogin();
    document.getElementById('username').value = username;
    setMessage(loginMessage, 'Account created. Please sign in.', false);
    document.getElementById('password').focus();
  } catch {
    setMessage(registerMessage, 'Registration failed. Please try again.', true);
  }
});

function openCreatePage() {
  dashboardSection.classList.add('hidden');
  createSection.classList.remove('hidden');
  setMessage(noticeMessage, '');
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  document.getElementById('publishedAt').value = now.toISOString().slice(0, 16);
  document.getElementById('expiryDate').value = new Date(Date.now() + 7 * 86400000).toISOString().slice(0, 10);
  document.getElementById('title').focus();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function closeCreatePage() {
  createSection.classList.add('hidden');
  dashboardSection.classList.remove('hidden');
  setMessage(noticeMessage, '');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.getElementById('openCreateButton').addEventListener('click', openCreatePage);
document.getElementById('closeCreateButton').addEventListener('click', closeCreatePage);
document.getElementById('cancelCreateButton').addEventListener('click', closeCreatePage);

document.getElementById('logoutButton').addEventListener('click', () => {
  authState = { username: '', password: '' };
  noticeForm.reset();
  dashboardSection.classList.add('hidden');
  createSection.classList.add('hidden');
  loginSection.classList.remove('hidden');
  showLogin();
  document.getElementById('password').value = '';
});

noticeForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (!authState.username || !authState.password) {
    setMessage(noticeMessage, 'Please sign in again before creating a notice.', true);
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

  submitNoticeButton.disabled = true;
  submitNoticeButton.textContent = 'Creating…';
  try {
    const response = await fetch('/api/admin/notices', {
      method: 'POST',
      headers: authHeaders(true),
      body: JSON.stringify(payload)
    });
    if (!response.ok) {
      setMessage(noticeMessage, await responseMessage(response, 'Notice creation failed.'), true);
      return;
    }
    noticeForm.reset();
    closeCreatePage();
    await loadNotices();
    setMessage(dashboardMessage, `“${payload.title}” was created successfully.`);
  } catch {
    setMessage(noticeMessage, 'Notice creation failed. Please try again.', true);
  } finally {
    submitNoticeButton.disabled = false;
    submitNoticeButton.textContent = 'Create notice';
  }
});
