// Configuração da API
const API_BASE = 'http://localhost:8080/api';

// ===== SEÇÕES =====
function showSection(sectionId) {
  document.querySelectorAll('.auth-section').forEach(section => {
    section.classList.remove('active');
  });
  document.getElementById(sectionId).classList.add('active');
  clearMessages();
}

// ===== MENSAGENS =====
function showMsg(elementId, message, type = 'error') {
  const element = document.getElementById(elementId);
  element.textContent = message;
  element.classList.add('show');

  const duration = type === 'success' ? 4000 : 8000;
  setTimeout(() => {
    element.classList.remove('show');
  }, duration);
}

function clearMessages() {
  document.querySelectorAll('.message').forEach(msg => {
    msg.classList.remove('show');
    msg.textContent = '';
  });
}

// ===== LOGIN (POST /api/login) =====
async function handleLogin(event) {
  event.preventDefault();

  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value.trim();

  if (!username || !password) {
    showMsg('loginError', 'Preencha todos os campos!');
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({ username, password })
    });

    if (response.ok) {
      const message = await response.text();
      showMsg('loginSuccess', message, 'success');
      document.getElementById('loginUsername').value = '';
      document.getElementById('loginPassword').value = '';
      
      updateNavigation(true);
      
      setTimeout(() => {
        showSection('profile');
      }, 1500);
    } else if (response.status === 401) {
      const error = await response.text();
      showMsg('loginError', error || 'Usuário ou senha inválidos');
    } else {
      showMsg('loginError', 'Erro ao fazer login');
    }
  } catch (error) {
    console.error('Erro:', error);
    showMsg('loginError', 'Erro de conexão. Verifique se o servidor está rodando em http://localhost:8080');
  }
}

// ===== REGISTRO (POST /api/registrar) =====
async function handleRegister(event) {
  event.preventDefault();

  const username = document.getElementById('regUsername').value.trim();
  const password = document.getElementById('regPassword').value.trim();
  const confirm = document.getElementById('regConfirm').value.trim();

  if (!username || !password || !confirm) {
    showMsg('registerError', 'Preencha todos os campos!');
    return;
  }

  if (username.length < 3) {
    showMsg('registerError', 'Usuário deve ter pelo menos 3 caracteres');
    return;
  }

  if (password.length < 6) {
    showMsg('registerError', 'Senha deve ter pelo menos 6 caracteres');
    return;
  }

  if (password !== confirm) {
    showMsg('registerError', 'As senhas não correspondem');
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/registrar`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({ username, password })
    });

    if (response.ok || response.status === 201) {
      const message = await response.text();
      showMsg('registerSuccess', 'Conta criada com sucesso! Redirecionando...', 'success');
      document.getElementById('regUsername').value = '';
      document.getElementById('regPassword').value = '';
      document.getElementById('regConfirm').value = '';

      setTimeout(() => {
        document.getElementById('loginUsername').value = username;
        showSection('login');
      }, 1500);
    } else if (response.status === 400) {
      const error = await response.text();
      showMsg('registerError', error || 'Username já existe ou dados inválidos');
    } else {
      showMsg('registerError', 'Erro ao criar conta');
    }
  } catch (error) {
    console.error('Erro:', error);
    showMsg('registerError', 'Erro de conexão. Verifique se o servidor está rodando');
  }
}

// ===== CARREGAR PERFIL (GET /api/meu-perfil) =====
async function loadProfile() {
  const loadingDiv = document.getElementById('profileLoading');
  const contentDiv = document.getElementById('profileContent');
  const errorDiv = document.getElementById('profileError');

  loadingDiv.style.display = 'flex';
  contentDiv.style.display = 'none';
  errorDiv.classList.remove('show');

  try {
    const response = await fetch(`${API_BASE}/meu-perfil`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (response.ok) {
      const data = await response.json();
      
      // Preenche os dados do perfil
      document.getElementById('profileName').textContent = data.username;
      document.getElementById('profileUsername').textContent = data.username;
      document.getElementById('profileEmail').textContent = data.email || 'Não fornecido';
      
      // Formata a data
      if (data.criadoEm) {
        const data_criacao = new Date(data.criadoEm);
        const dataFormatada = data_criacao.toLocaleDateString('pt-BR', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
        document.getElementById('profileDate').textContent = dataFormatada;
      }

      loadingDiv.style.display = 'none';
      contentDiv.style.display = 'block';
    } else if (response.status === 401) {
      loadingDiv.style.display = 'none';
      errorDiv.textContent = 'Sessão expirada. Faça login novamente';
      errorDiv.classList.add('show');
      updateNavigation(false);
      setTimeout(() => {
        showSection('login');
      }, 2000);
    } else {
      throw new Error('Erro ao carregar perfil');
    }
  } catch (error) {
    console.error('Erro:', error);
    loadingDiv.style.display = 'none';
    errorDiv.textContent = 'Erro ao carregar perfil. Tente novamente';
    errorDiv.classList.add('show');
  }
}

// ===== LOGOUT (POST /api/logout) =====
async function doLogout(event) {
  if (event) {
    event.preventDefault();
  }

  try {
    const response = await fetch(`${API_BASE}/logout`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    // Independente da resposta, desautentica o usuário
    updateNavigation(false);
    showSection('login');
    clearMessages();
  } catch (error) {
    console.error('Erro ao fazer logout:', error);
    updateNavigation(false);
    showSection('login');
  }
}

// ===== NAVEGAÇÃO =====
function updateNavigation(isLoggedIn) {
  const profileNav = document.getElementById('navProfile');
  const logoutNav = document.getElementById('navLogout');
  const loginNav = document.getElementById('navLogin');
  const registerNav = document.getElementById('navRegister');

  if (isLoggedIn) {
    profileNav.style.display = 'block';
    logoutNav.style.display = 'block';
    loginNav.style.display = 'none';
    registerNav.style.display = 'none';
  } else {
    profileNav.style.display = 'none';
    logoutNav.style.display = 'none';
    loginNav.style.display = 'block';
    registerNav.style.display = 'block';
  }
}

// ===== INICIALIZAÇÃO =====
document.addEventListener('DOMContentLoaded', () => {
  updateNavigation(false);

  // Menu hamburger mobile
  const hamburger = document.getElementById('hamburger');
  const navMenu = document.getElementById('navMenu');

  if (hamburger) {
    hamburger.addEventListener('click', () => {
      navMenu.style.display = navMenu.style.display === 'none' || navMenu.style.display === '' ? 'flex' : 'none';
    });
  }

  // Fechar menu ao clicar em um link
  document.querySelectorAll('.navbar-menu a').forEach(link => {
    link.addEventListener('click', () => {
      if (navMenu) navMenu.style.display = 'none';
    });
  });
});
