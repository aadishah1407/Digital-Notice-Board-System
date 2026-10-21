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
const expiryDateInput = document.getElementById('expiryDate');
const publishedAtInput = document.getElementById('publishedAt');

let authState = { username: '', password: '' };
let editingNoticeId = null;

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
    const actions = document.createElement('div');
    actions.className = 'notice-actions';
    const editButton = document.createElement('button');
    editButton.type = 'button';
    editButton.className = 'notice-action-button';
    editButton.dataset.action = 'edit';
    editButton.dataset.noticeId = notice.id;
    editButton.textContent = 'Edit';
    const deleteButton = document.createElement('button');
    deleteButton.type = 'button';
    deleteButton.className = 'notice-action-button delete-button';
    deleteButton.dataset.action = 'delete';
    deleteButton.dataset.noticeId = notice.id;
    deleteButton.textContent = 'Delete';
    actions.append(editButton, deleteButton);
    article.append(heading, description, meta, actions);
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
    return true;
  } catch (error) {
    setMessage(dashboardMessage, error.message || 'Could not load notices. Please try again.', true);
    return false;
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
  noticeForm.reset();
  editingNoticeId = null;
  document.getElementById('formEyebrow').textContent = 'NEW ANNOUNCEMENT';
  document.getElementById('createHeading').textContent = 'Create a notice';
  submitNoticeButton.textContent = 'Create notice';
  setMessage(noticeMessage, '');
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  publishedAtInput.value = now.toISOString().slice(0, 16);
  expiryDateInput.value = new Date(Date.now() + 7 * 86400000).toISOString().slice(0, 10);
  updateExpiryDateValidity();
  document.getElementById('title').focus();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function closeCreatePage() {
  createSection.classList.add('hidden');
  dashboardSection.classList.remove('hidden');
  editingNoticeId = null;
  noticeForm.reset();
  setMessage(noticeMessage, '');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function updateExpiryDateValidity() {
  const publicationDate = publishedAtInput.value.slice(0, 10);
  const isValid = expiryDateInput.value && publicationDate && expiryDateInput.value > publicationDate;
  expiryDateInput.setCustomValidity(isValid ? '' : 'Expiry date must be later than the publication date.');
  if (expiryDateInput.value && publicationDate && !isValid) {
    setMessage(noticeMessage, 'Expiry date must be later than the publication date.', true);
  } else if (noticeMessage.textContent === 'Expiry date must be later than the publication date.') {
    setMessage(noticeMessage, '');
  }
}

function openEditPage(notice) {
  dashboardSection.classList.add('hidden');
  createSection.classList.remove('hidden');
  noticeForm.reset();
  editingNoticeId = notice.id;
  document.getElementById('formEyebrow').textContent = 'EDIT ANNOUNCEMENT';
  document.getElementById('createHeading').textContent = 'Edit notice';
  submitNoticeButton.textContent = 'Save changes';
  document.getElementById('title').value = notice.title;
  document.getElementById('description').value = notice.description;
  document.getElementById('department').value = notice.department;
  document.getElementById('academicYear').value = notice.academicYear;
  publishedAtInput.value = notice.publishedAt.slice(0, 16);
  expiryDateInput.value = notice.expiryDate;
  setMessage(noticeMessage, '');
  updateExpiryDateValidity();
  document.getElementById('title').focus();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.getElementById('openCreateButton').addEventListener('click', openCreatePage);
document.getElementById('closeCreateButton').addEventListener('click', closeCreatePage);
document.getElementById('cancelCreateButton').addEventListener('click', closeCreatePage);
publishedAtInput.addEventListener('input', updateExpiryDateValidity);
expiryDateInput.addEventListener('input', updateExpiryDateValidity);

noticeList.addEventListener('click', async (event) => {
  const button = event.target.closest('button[data-action]');
  if (!button) return;

  const noticeId = button.dataset.noticeId;
  if (button.dataset.action === 'edit') {
    try {
      const response = await fetch('/api/admin/notices', { headers: authHeaders() });
      if (!response.ok) throw new Error(await responseMessage(response, 'Could not load the selected notice.'));
      const notices = await response.json();
      const notice = notices.find((item) => String(item.id) === noticeId);
      if (!notice) throw new Error('This notice no longer exists. Refresh the dashboard and try again.');
      openEditPage(notice);
    } catch (error) {
      setMessage(dashboardMessage, error.message || 'Could not load the selected notice.', true);
    }
    return;
  }

  if (button.dataset.action === 'delete') {
    const noticeCard = button.closest('.notice-card');
    const title = noticeCard.querySelector('h3').textContent;
    if (!window.confirm(`Delete "${title}"? This action cannot be undone.`)) return;

    button.disabled = true;
    try {
      const response = await fetch(`/api/admin/notices/${encodeURIComponent(noticeId)}`, {
        method: 'DELETE',
        headers: authHeaders()
      });
      if (!response.ok) throw new Error(await responseMessage(response, 'Could not delete the notice.'));
      if (await loadNotices()) {
        setMessage(dashboardMessage, `"${title}" was deleted successfully.`);
      }
    } catch (error) {
      button.disabled = false;
      setMessage(dashboardMessage, error.message || 'Could not delete the notice.', true);
    }
  }
});

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

  updateExpiryDateValidity();
  if (!expiryDateInput.reportValidity()) return;

  submitNoticeButton.disabled = true;
  submitNoticeButton.textContent = editingNoticeId ? 'Saving…' : 'Creating…';
  try {
    const isEditing = editingNoticeId !== null;
    const response = await fetch(
      isEditing ? `/api/admin/notices/${encodeURIComponent(editingNoticeId)}` : '/api/admin/notices',
      {
        method: isEditing ? 'PUT' : 'POST',
        headers: authHeaders(true),
        body: JSON.stringify(payload)
      }
    );
    if (!response.ok) {
      setMessage(noticeMessage, await responseMessage(response, isEditing ? 'Notice update failed.' : 'Notice creation failed.'), true);
      return;
    }
    noticeForm.reset();
    editingNoticeId = null;
    closeCreatePage();
    if (await loadNotices()) {
      setMessage(dashboardMessage, `“${payload.title}” was ${isEditing ? 'updated' : 'created'} successfully.`);
    }
  } catch {
    setMessage(noticeMessage, editingNoticeId ? 'Notice update failed. Please try again.' : 'Notice creation failed. Please try again.', true);
  } finally {
    submitNoticeButton.disabled = false;
    submitNoticeButton.textContent = editingNoticeId ? 'Save changes' : 'Create notice';
  }
});
