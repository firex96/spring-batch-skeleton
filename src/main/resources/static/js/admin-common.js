/**
 * Admin Dashboard Common Utilities
 */

// API Key Store
// sessionStorage 사용 — 탭 종료 시 소멸한다. localStorage 는 공용 PC 에 키가 남는다.
const ApiKeyStore = {
    STORAGE_KEY: 'batch-admin-api-key',

    get() {
        return sessionStorage.getItem(this.STORAGE_KEY);
    },

    set(value) {
        sessionStorage.setItem(this.STORAGE_KEY, value);
    },

    clear() {
        sessionStorage.removeItem(this.STORAGE_KEY);
    },

    has() {
        const value = this.get();
        return value !== null && value !== '';
    },

    /** 화면 표시용 — 마지막 4자만 남긴다. */
    masked() {
        const value = this.get();
        if (!value) return null;
        return value.length <= 4 ? '••••' : '••••' + value.slice(-4);
    }
};

// API Utility
const AdminApi = {
    baseUrl: '/admin/api',

    /** 401 재입력 유도를 요청마다 띄우지 않기 위한 1회 플래그. */
    _unauthorizedNotified: false,

    _headers(extra = {}) {
        const headers = Object.assign({ 'Accept': 'application/json' }, extra);
        const apiKey = ApiKeyStore.get();
        if (apiKey) {
            headers['X-API-Key'] = apiKey;
        }
        return headers;
    },

    /**
     * 실패 응답을 status 를 실은 Error 로 변환한다.
     * 호출부가 401/403/400 을 구분하려면 메시지 파싱이 아니라 error.status 를 봐야 한다.
     */
    _toError(response) {
        const error = new Error(`HTTP error! status: ${response.status}`);
        error.status = response.status;
        return error;
    },

    /**
     * 401 처리.
     *
     * 키를 **보낸 적이 있는데** 거부당한 경우에만 폐기 + 재입력을 유도한다(1회).
     * 키 미설정 상태의 401 은 정상적인 초기 상태이므로 조용히 둔다 — 배지가 이미 원인을 상시 표시하고,
     * 페이지 로드마다 실패 toast 를 띄우면 소음만 커진다.
     */
    _handleUnauthorized() {
        if (!ApiKeyStore.has()) return;

        ApiKeyStore.clear();
        if (this._unauthorizedNotified) return;
        this._unauthorizedNotified = true;
        if (typeof Toast !== 'undefined') {
            Toast.error('API Key 인증 실패 — 다시 입력하세요.');
        }
        if (typeof ApiKeyUi !== 'undefined') {
            ApiKeyUi.refreshBadge();
            ApiKeyUi.openModal();
        }
    },

    async get(endpoint, params = {}) {
        const url = new URL(this.baseUrl + endpoint, window.location.origin);
        Object.keys(params).forEach(key => {
            if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
                url.searchParams.append(key, params[key]);
            }
        });

        try {
            const response = await fetch(url.toString(), {
                method: 'GET',
                headers: this._headers()
            });

            if (!response.ok) {
                if (response.status === 401) this._handleUnauthorized();
                throw this._toError(response);
            }

            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    async post(endpoint, data = {}) {
        const url = this.baseUrl + endpoint;

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: this._headers({ 'Content-Type': 'application/json' }),
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                if (response.status === 401) this._handleUnauthorized();
                throw this._toError(response);
            }

            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }
};

// API Key UI — layout.html 의 navbar 배지 + 입력 Modal 을 다룬다(전 페이지 공용).
const ApiKeyUi = {
    refreshBadge() {
        const badge = document.getElementById('api-key-badge');
        const button = document.getElementById('api-key-btn');
        if (!badge || !button) return;

        if (ApiKeyStore.has()) {
            badge.className = 'badge bg-secondary me-2';
            badge.innerHTML = `<i class="bi bi-key-fill me-1"></i>${ApiKeyStore.masked()}`;
            button.textContent = '변경';
        } else {
            badge.className = 'badge bg-warning text-dark me-2';
            badge.innerHTML = '<i class="bi bi-lock-fill me-1"></i>API Key 미설정';
            button.textContent = '키 설정';
        }
        document.dispatchEvent(new CustomEvent('apikey:changed'));
    },

    openModal() {
        const el = document.getElementById('api-key-modal');
        if (!el) return;
        bootstrap.Modal.getOrCreateInstance(el).show();
    },

    save() {
        const input = document.getElementById('api-key-input');
        if (!input || !input.value) return;
        ApiKeyStore.set(input.value);
        AdminApi._unauthorizedNotified = false;
        input.value = '';
        const el = document.getElementById('api-key-modal');
        if (el) bootstrap.Modal.getOrCreateInstance(el).hide();
        this.refreshBadge();
        location.reload();
    }
};

// Date Utility
const DateUtil = {
    format(dateString) {
        if (!dateString) return '-';

        const date = new Date(dateString);
        return date.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    },

    formatDate(dateString) {
        if (!dateString) return '-';

        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    },

    formatDuration(startTime, endTime) {
        if (!startTime) return '-';
        if (!endTime) return 'Running...';

        const start = new Date(startTime);
        const end = new Date(endTime);
        const durationMs = end - start;

        if (durationMs < 0) return '-';

        if (durationMs < 1000) {
            return `${durationMs}ms`;
        } else if (durationMs < 60000) {
            return `${(durationMs / 1000).toFixed(1)}s`;
        } else if (durationMs < 3600000) {
            const minutes = Math.floor(durationMs / 60000);
            const seconds = Math.floor((durationMs % 60000) / 1000);
            return `${minutes}m ${seconds}s`;
        } else {
            const hours = Math.floor(durationMs / 3600000);
            const minutes = Math.floor((durationMs % 3600000) / 60000);
            return `${hours}h ${minutes}m`;
        }
    },

    formatDurationMs(ms) {
        if (ms === null || ms === undefined) return '-';

        if (ms < 1000) {
            return `${ms}ms`;
        } else if (ms < 60000) {
            return `${(ms / 1000).toFixed(1)}s`;
        } else if (ms < 3600000) {
            const minutes = Math.floor(ms / 60000);
            const seconds = Math.floor((ms % 60000) / 1000);
            return `${minutes}m ${seconds}s`;
        } else {
            const hours = Math.floor(ms / 3600000);
            const minutes = Math.floor((ms % 3600000) / 60000);
            return `${hours}h ${minutes}m`;
        }
    }
};

// Status Badge Utility
const StatusBadge = {
    getClass(status) {
        const statusMap = {
            'COMPLETED': 'bg-success',
            'FAILED': 'bg-danger',
            'STARTED': 'bg-info',
            'STARTING': 'bg-warning',
            'STOPPED': 'bg-secondary',
            'STOPPING': 'bg-warning',
            'ABANDONED': 'bg-dark',
            'UNKNOWN': 'bg-secondary'
        };
        return statusMap[status] || 'bg-secondary';
    },

    render(status) {
        const badgeClass = this.getClass(status);
        return `<span class="badge ${badgeClass}">${status}</span>`;
    }
};

// Toast Notification
const Toast = {
    show(message, type = 'info') {
        // Create toast container if not exists
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            container.style.zIndex = '1100';
            document.body.appendChild(container);
        }

        // Create toast
        const toastId = 'toast-' + Date.now();
        const bgClass = {
            'success': 'bg-success',
            'error': 'bg-danger',
            'warning': 'bg-warning',
            'info': 'bg-info'
        }[type] || 'bg-info';

        const toastHtml = `
            <div id="${toastId}" class="toast ${bgClass} text-white" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-body d-flex justify-content-between align-items-center">
                    ${message}
                    <button type="button" class="btn-close btn-close-white ms-2" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', toastHtml);

        const toastElement = document.getElementById(toastId);
        const bsToast = new bootstrap.Toast(toastElement, { delay: 3000 });
        bsToast.show();

        // Remove from DOM after hidden
        toastElement.addEventListener('hidden.bs.toast', function() {
            toastElement.remove();
        });
    },

    success(message) {
        this.show(message, 'success');
    },

    error(message) {
        this.show(message, 'error');
    },

    warning(message) {
        this.show(message, 'warning');
    },

    info(message) {
        this.show(message, 'info');
    }
};
