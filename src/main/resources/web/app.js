const loginView = document.getElementById('login-view');
const appView = document.getElementById('app-view');
const alertBox = document.getElementById('alert');
const loginForm = document.getElementById('login-form');
const customerForm = document.getElementById('customer-form');
const refreshBtn = document.getElementById('refresh-btn');
const logoutBtn = document.getElementById('logout-btn');
const cancelEditBtn = document.getElementById('cancel-edit');
const tableBody = document.getElementById('customer-table');
const formTitle = document.getElementById('form-title');

let authToken = null;

function showAlert(message, type = 'error') {
    if (!message) {
        alertBox.classList.add('hidden');
        alertBox.textContent = '';
        return;
    }
    const styles = {
        error: 'border-red-200 bg-red-50 text-red-600',
        success: 'border-emerald-200 bg-emerald-50 text-emerald-600',
    };
    alertBox.className = `mb-6 rounded-lg px-4 py-3 text-sm ${styles[type] ?? styles.error}`;
    alertBox.textContent = message;
}

function toggleViews(loggedIn) {
    if (loggedIn) {
        loginView.classList.add('hidden');
        appView.classList.remove('hidden');
    } else {
        loginView.classList.remove('hidden');
        appView.classList.add('hidden');
    }
}

async function fetchWithAuth(url, options = {}) {
    const headers = options.headers ? {...options.headers} : {};
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }
    if (options.body && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }
    const response = await fetch(url, {...options, headers});
    if (!response.ok) {
        let errorMessage = response.statusText;
        try {
            const data = await response.json();
            errorMessage = data.error || errorMessage;
        } catch (_) {
            // ignore parse errors
        }
        throw new Error(errorMessage);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

function renderCustomers(customers) {
    tableBody.innerHTML = '';
    if (!customers || customers.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = `<td colspan="4" class="px-4 py-4 text-center text-slate-400">Nenhum cliente encontrado.</td>`;
        tableBody.appendChild(row);
        return;
    }

    customers.forEach(customer => {
        const row = document.createElement('tr');
        row.className = 'hover:bg-slate-50';
        row.innerHTML = `
            <td class="px-4 py-3 text-slate-500">${customer.id}</td>
            <td class="px-4 py-3">${customer.name}</td>
            <td class="px-4 py-3">${customer.email}</td>
            <td class="px-4 py-3 text-center">
                <button class="edit-btn rounded-lg border border-sky-200 px-3 py-1 text-sm font-medium text-sky-600 transition hover:bg-sky-50">Editar</button>
                <button class="delete-btn ml-2 rounded-lg border border-red-200 px-3 py-1 text-sm font-medium text-red-500 transition hover:bg-red-50">Excluir</button>
            </td>
        `;
        row.querySelector('.edit-btn').addEventListener('click', () => startEdit(customer));
        row.querySelector('.delete-btn').addEventListener('click', () => deleteCustomer(customer.id));
        tableBody.appendChild(row);
    });
}

function resetForm() {
    customerForm.reset();
    document.getElementById('customer-id').value = '';
    cancelEditBtn.classList.add('hidden');
    formTitle.textContent = 'Novo cliente';
}

function startEdit(customer) {
    document.getElementById('customer-id').value = customer.id;
    document.getElementById('customer-name').value = customer.name;
    document.getElementById('customer-email').value = customer.email;
    cancelEditBtn.classList.remove('hidden');
    formTitle.textContent = `Editar cliente #${customer.id}`;
}

async function loadCustomers() {
    try {
        showAlert('');
        const data = await fetchWithAuth('/api/customers');
        renderCustomers(data);
    } catch (error) {
        showAlert(error.message);
    }
}

async function deleteCustomer(id) {
    if (!confirm('Deseja excluir este cliente?')) {
        return;
    }
    try {
        showAlert('');
        await fetchWithAuth(`/api/customers/${id}`, { method: 'DELETE' });
        resetForm();
        await loadCustomers();
        showAlert('Cliente removido com sucesso!', 'success');
    } catch (error) {
        showAlert(error.message);
    }
}

loginForm.addEventListener('submit', async event => {
    event.preventDefault();
    const formData = new FormData(loginForm);
    const payload = Object.fromEntries(formData.entries());
    try {
        showAlert('');
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || 'Falha no login');
        }
        const data = await response.json();
        authToken = data.token;
        toggleViews(true);
        await loadCustomers();
        loginForm.reset();
    } catch (error) {
        showAlert(error.message);
    }
});

customerForm.addEventListener('submit', async event => {
    event.preventDefault();
    const id = document.getElementById('customer-id').value;
    const name = document.getElementById('customer-name').value.trim();
    const email = document.getElementById('customer-email').value.trim();

    if (!name || !email) {
        showAlert('Informe nome e e-mail válidos.');
        return;
    }

    const payload = { name, email };
    const options = {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify(payload)
    };

    try {
        showAlert('');
        const url = id ? `/api/customers/${id}` : '/api/customers';
        await fetchWithAuth(url, options);
        resetForm();
        await loadCustomers();
        showAlert('Cliente salvo com sucesso!', 'success');
    } catch (error) {
        showAlert(error.message);
    }
});

refreshBtn.addEventListener('click', loadCustomers);

logoutBtn.addEventListener('click', () => {
    authToken = null;
    toggleViews(false);
    showAlert('Sessão encerrada.', 'success');
});

cancelEditBtn.addEventListener('click', () => {
    resetForm();
});

// Estado inicial
toggleViews(false);
showAlert('');
renderCustomers([]);
