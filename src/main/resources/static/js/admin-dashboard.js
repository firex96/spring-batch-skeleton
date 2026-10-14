/**
 * Admin Dashboard Page Script
 */

let dailyStatsChart = null;

document.addEventListener('DOMContentLoaded', function() {
    loadDashboardSummary();
    loadDailyStats();
    loadJobs();
    loadRecentExecutions();

    // Job trigger button handler
    // 활성 조건: Job 선택 AND API Key 설정. 툴팁은 키 미설정을 우선 표시한다(선행 조치이므로).
    document.getElementById('job-select').addEventListener('change', refreshTriggerButton);
    document.addEventListener('apikey:changed', refreshTriggerButton);
    refreshTriggerButton();

    document.getElementById('trigger-btn').addEventListener('click', triggerJob);

    // Auto-refresh every 30 seconds
    setInterval(function() {
        loadDashboardSummary();
        loadRecentExecutions();
    }, 30000);
});

async function loadDashboardSummary() {
    try {
        const summary = await AdminApi.get('/dashboard/summary');

        document.getElementById('total-jobs').textContent = summary.totalJobs;
        document.getElementById('total-executions-today').textContent = summary.totalExecutionsToday;
        document.getElementById('completed-today').textContent = summary.completedToday;
        document.getElementById('failed-today').textContent = summary.failedToday;
        document.getElementById('running-now').textContent = summary.runningNow;
        document.getElementById('scheduled-jobs').textContent = summary.scheduledJobs;
    } catch (error) {
        console.error('Failed to load dashboard summary:', error);
    }
}

async function loadDailyStats() {
    try {
        const stats = await AdminApi.get('/dashboard/daily-stats', { days: 7 });
        renderDailyStatsChart(stats);
    } catch (error) {
        console.error('Failed to load daily stats:', error);
    }
}

function renderDailyStatsChart(stats) {
    const ctx = document.getElementById('dailyStatsChart').getContext('2d');

    // Destroy existing chart if exists
    if (dailyStatsChart) {
        dailyStatsChart.destroy();
    }

    const labels = stats.map(s => {
        const date = new Date(s.date);
        return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
    });

    const completedData = stats.map(s => s.completedCount);
    const failedData = stats.map(s => s.failedCount);
    const totalData = stats.map(s => s.totalExecutions);

    dailyStatsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Completed',
                    data: completedData,
                    backgroundColor: 'rgba(25, 135, 84, 0.8)',
                    borderColor: 'rgb(25, 135, 84)',
                    borderWidth: 1
                },
                {
                    label: 'Failed',
                    data: failedData,
                    backgroundColor: 'rgba(220, 53, 69, 0.8)',
                    borderColor: 'rgb(220, 53, 69)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        afterLabel: function(context) {
                            const idx = context.dataIndex;
                            return `Total: ${totalData[idx]}`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    stacked: true,
                    grid: {
                        display: false
                    }
                },
                y: {
                    stacked: true,
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

async function loadJobs() {
    try {
        const jobs = await AdminApi.get('/jobs');
        const select = document.getElementById('job-select');

        jobs.forEach(job => {
            const option = document.createElement('option');
            option.value = job;
            option.textContent = job;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Failed to load jobs:', error);
    }
}

async function loadRecentExecutions() {
    try {
        const executions = await AdminApi.get('/executions', { page: 0, size: 5 });
        renderRecentExecutions(executions);
    } catch (error) {
        console.error('Failed to load recent executions:', error);
    }
}

function renderRecentExecutions(executions) {
    const container = document.getElementById('recent-executions');

    if (executions.length === 0) {
        container.innerHTML = `
            <div class="list-group-item text-center text-muted py-4">
                <i class="bi bi-inbox"></i> No recent executions
            </div>
        `;
        return;
    }

    container.innerHTML = executions.map(exec => `
        <a href="/admin/executions/${exec.jobExecutionId}" class="list-group-item list-group-item-action hover-bg-light">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <strong>${exec.jobName}</strong>
                    <br>
                    <small class="text-muted">${DateUtil.format(exec.startTime)}</small>
                </div>
                ${StatusBadge.render(exec.status)}
            </div>
        </a>
    `).join('');
}

function refreshTriggerButton() {
    const select = document.getElementById('job-select');
    const btn = document.getElementById('trigger-btn');
    if (!select || !btn) return;

    const hasKey = ApiKeyStore.has();
    const hasJob = !!select.value;
    btn.disabled = !hasJob || !hasKey;
    btn.title = !hasKey ? 'API Key 를 먼저 설정하세요' : (!hasJob ? 'Job 을 선택하세요' : '');
}

async function triggerJob() {
    const jobName = document.getElementById('job-select').value;
    if (!jobName) return;

    const btn = document.getElementById('trigger-btn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Triggering...';

    try {
        const result = await AdminApi.post(`/jobs/${jobName}/trigger`, {});

        document.getElementById('trigger-execution-id').textContent = result.jobExecutionId;
        document.getElementById('trigger-trace-id').textContent = result.traceId;
        document.getElementById('trigger-result').style.display = 'block';

        Toast.success(`Job "${jobName}" triggered successfully!`);

        // Refresh data
        loadDashboardSummary();
        loadRecentExecutions();
    } catch (error) {
        // 401 은 AdminApi 가 키 폐기 + 재입력 유도까지 처리했으므로 여기서 중복 알리지 않는다.
        if (error.status === 403) {
            Toast.error('이 키로는 실행할 수 없는 Job 입니다.');
        } else if (error.status === 400) {
            Toast.error('허용되지 않은 파라미터입니다.');
        } else if (error.status !== 401) {
            Toast.error(`Failed to trigger job: ${error.message}`);
        }
    } finally {
        refreshTriggerButton();
        btn.innerHTML = '<i class="bi bi-play-fill me-1"></i>Trigger Job';
    }
}
