const studentAuthSection = document.getElementById('studentAuthSection');
const studentBoardSection = document.getElementById('studentBoardSection');
const studentLoginForm = document.getElementById('studentLoginForm');
const studentRegisterForm = document.getElementById('studentRegisterForm');
const studentNoticeList = document.getElementById('studentNoticeList');
const studentLoginMessage = document.getElementById('studentLoginMessage');
const studentRegisterMessage = document.getElementById('studentRegisterMessage');
const studentBoardMessage = document.getElementById('studentBoardMessage');
let studentAuth = { username: '', password: '' };

function showMessage(element, message, isError = false) {
  element.textContent = message;
  element.className = `message${message ? ` ${isError ? 'error' : 'success'}` : ''}`;
}

function studentHeaders() {
  return { Authorization: `Basic ${btoa(`${studentAuth.username}:${studentAuth.password}`)}` };
}

async function getError(response, fallback) {
  try {
    const body = await response.json();
    return body.message || body.detail || fallback;
  } catch {
    return fallback;
  }
}

function switchStudentForm(showRegister) {
  studentLoginForm.classList.toggle('hidden', showRegister);
  studentRegisterForm.classList.toggle('hidden', !showRegister);
  document.getElementById('studentAuthSwitch').classList.toggle('hidden', showRegister);
  document.getElementById('studentAuthHeading').textContent = showRegister ? 'Create a student account' : 'Student sign in';
  document.getElementById('studentAuthCopy').textContent = showRegister
    ? 'Register with your department and academic year to see relevant notices.'
    : 'Sign in to see notices relevant to you.';
  showMessage(studentLoginMessage, '');
  showMessage(studentRegisterMessage, '');
}

function renderStudentNotices(notices) {
  studentNoticeList.replaceChildren();
  if (!notices.length) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.innerHTML = '<div class="empty-icon" aria-hidden="true">✦</div><h3>No active notices</h3><p>There are no current notices for your department and academic year.</p>';
    studentNoticeList.append(empty);
    return;
  }

  notices.forEach((notice) => {
    const card = document.createElement('article');
    card.className = 'notice-card';
    const heading = document.createElement('div');
    heading.className = 'notice-card-heading';
    const title = document.createElement('h3');
    title.textContent = notice.title;
    const department = document.createElement('span');
    department.className = 'notice-tag';
    department.textContent = notice.department;
    heading.append(title, department);
    const description = document.createElement('p');
    description.className = 'notice-description';
    description.textContent = notice.description;
    const meta = document.createElement('div');
    meta.className = 'notice-meta';
    const published = document.createElement('span');
    published.textContent = `Published ${new Date(notice.publishedAt).toLocaleString()}`;
    const expires = document.createElement('span');
    expires.textContent = `Expires ${new Date(`${notice.expiryDate}T00:00:00`).toLocaleDateString()}`;
    meta.append(published, expires);
    card.append(heading, description, meta);
    studentNoticeList.append(card);
  });
}

async function loadStudentNotices() {
  showMessage(studentBoardMessage, 'Loading notices…');
  try {
    const query = new URLSearchParams();
    const department = document.getElementById('departmentFilter').value;
    const academicYear = document.getElementById('academicYearFilter').value;
    if (department) query.set('department', department);
    if (academicYear) query.set('academicYear', academicYear);
    const queryString = query.toString();
    const suffix = queryString ? `?${queryString}` : '';
    const response = await fetch(`/api/student/notices${suffix}`, { headers: studentHeaders() });
    if (!response.ok) throw new Error(await getError(response, 'Could not load student notices.'));
    renderStudentNotices(await response.json());
    showMessage(studentBoardMessage, '');
  } catch (error) {
    showMessage(studentBoardMessage, error.message || 'Could not load student notices.', true);
  }
}

function fillOptions(select, values, allLabel, preferredValue) {
  select.replaceChildren(new Option(allLabel, ''));
  values.forEach((value) => select.add(new Option(value, value)));
  if (preferredValue && values.some((value) => value.toLowerCase() === preferredValue.toLowerCase())) {
    select.value = values.find((value) => value.toLowerCase() === preferredValue.toLowerCase());
  }
}

async function loadStudentFilters(result) {
  const response = await fetch('/api/student/notices/filters', { headers: studentHeaders() });
  if (!response.ok) throw new Error(await getError(response, 'Could not load notice filters.'));
  const filters = await response.json();
  fillOptions(document.getElementById('departmentFilter'), filters.departments || [], 'All departments', result.department);
  fillOptions(document.getElementById('academicYearFilter'), filters.academicYears || [], 'All years', result.academicYear);
}

document.getElementById('showStudentRegister').addEventListener('click', () => switchStudentForm(true));
document.getElementById('showStudentLogin').addEventListener('click', () => switchStudentForm(false));

studentLoginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const username = document.getElementById('studentUsername').value.trim();
  const password = document.getElementById('studentPassword').value;
  try {
    const response = await fetch('/api/auth/student/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const result = await response.json();
    if (!response.ok || !result.authenticated) {
      showMessage(studentLoginMessage, result.message || 'Invalid student username or password.', true);
      return;
    }
    studentAuth = { username, password };
    studentAuthSection.classList.add('hidden');
    studentBoardSection.classList.remove('hidden');
    document.getElementById('studentSessionMessage').textContent = `Signed in as ${result.username}`;
    document.getElementById('studentFilterDescription').textContent = 'Choose a department, academic year, or both to narrow the notices.';
    await loadStudentFilters(result);
    await loadStudentNotices();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch {
    showMessage(studentLoginMessage, 'Sign in failed. Please try again.', true);
  }
});

document.getElementById('studentFilterForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  await loadStudentNotices();
});

document.getElementById('clearStudentFilters').addEventListener('click', async () => {
  document.getElementById('studentFilterForm').reset();
  await loadStudentNotices();
});

studentRegisterForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const registration = {
    username: document.getElementById('newStudentUsername').value.trim(),
    password: document.getElementById('newStudentPassword').value,
    department: document.getElementById('studentDepartment').value.trim(),
    academicYear: document.getElementById('studentAcademicYear').value.trim()
  };
  try {
    const response = await fetch('/api/auth/student/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(registration)
    });
    if (!response.ok) {
      showMessage(studentRegisterMessage, await getError(response, 'Student registration failed.'), true);
      return;
    }
    studentRegisterForm.reset();
    switchStudentForm(false);
    document.getElementById('studentUsername').value = registration.username;
    showMessage(studentLoginMessage, 'Student account created. Please sign in.');
    document.getElementById('studentPassword').focus();
  } catch {
    showMessage(studentRegisterMessage, 'Student registration failed. Please try again.', true);
  }
});

document.getElementById('studentLogoutButton').addEventListener('click', () => {
  studentAuth = { username: '', password: '' };
  studentBoardSection.classList.add('hidden');
  studentAuthSection.classList.remove('hidden');
  studentLoginForm.reset();
  studentRegisterForm.reset();
  switchStudentForm(false);
  showMessage(studentBoardMessage, '');
  window.scrollTo({ top: 0, behavior: 'smooth' });
});
