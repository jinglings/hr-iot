/**
 * SCADA Performance Load Test Script
 * 
 * Part of SCADA-030: Performance Load Testing
 * 
 * This script uses k6 for load testing the SCADA system.
 * Install k6: https://k6.io/docs/getting-started/installation/
 * 
 * Usage:
 *   k6 run tests/scada/load-test.js
 *   k6 run --vus 100 --duration 5m tests/scada/load-test.js
 * 
 * @author HR-IoT Team
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';

// ==========================================
// Configuration
// ==========================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PREFIX = '/admin-api/iot/scada';
const TOKEN = __ENV.AUTH_TOKEN || 'your-test-token';

// Test options
export const options = {
    // Stages for ramping up and down
    stages: [
        { duration: '1m', target: 20 },   // Ramp up to 20 users
        { duration: '3m', target: 50 },   // Ramp up to 50 users
        { duration: '5m', target: 100 },  // Ramp up to 100 users
        { duration: '5m', target: 100 },  // Stay at 100 users
        { duration: '2m', target: 50 },   // Ramp down to 50
        { duration: '1m', target: 0 },    // Ramp down to 0
    ],

    // Thresholds for pass/fail criteria
    thresholds: {
        http_req_duration: ['p(95)<3000', 'p(99)<5000'], // 95% < 3s, 99% < 5s
        http_req_failed: ['rate<0.01'],                   // <1% errors
        'dashboard_load_time': ['p(95)<3000'],           // Dashboard < 3s
        'device_list_time': ['p(95)<2000'],              // Device list < 2s
        'alarm_list_time': ['p(95)<1000'],               // Alarm list < 1s
    },
};

// ==========================================
// Custom Metrics
// ==========================================

const dashboardLoadTime = new Trend('dashboard_load_time');
const deviceListTime = new Trend('device_list_time');
const alarmListTime = new Trend('alarm_list_time');
const controlCommandTime = new Trend('control_command_time');
const errorRate = new Rate('error_rate');
const successfulLogins = new Counter('successful_logins');

// ==========================================
// Headers
// ==========================================

const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`,
    'tenant-id': '1',
};

// ==========================================
// Test Scenarios
// ==========================================

export default function () {

    // Scenario 1: Dashboard Loading
    group('Dashboard Loading', function () {
        // Get dashboard list
        let dashboardListRes = http.get(`${BASE_URL}${API_PREFIX}/dashboards`, { headers });
        dashboardLoadTime.add(dashboardListRes.timings.duration);

        check(dashboardListRes, {
            'dashboard list status is 200': (r) => r.status === 200,
            'dashboard list has data': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.data && Array.isArray(body.data);
                } catch {
                    return false;
                }
            },
        });

        // Get specific dashboard
        let dashboardRes = http.get(`${BASE_URL}${API_PREFIX}/dashboard/water_pump_system`, { headers });
        check(dashboardRes, {
            'dashboard detail status is 200': (r) => r.status === 200,
        });

        sleep(1);
    });

    // Scenario 2: Device Monitoring
    group('Device Monitoring', function () {
        // Get device list
        let deviceListRes = http.get(`${BASE_URL}${API_PREFIX}/devices?pageNo=1&pageSize=20`, { headers });
        deviceListTime.add(deviceListRes.timings.duration);

        check(deviceListRes, {
            'device list status is 200': (r) => r.status === 200,
            'device list response time < 2s': (r) => r.timings.duration < 2000,
        });

        // Get device properties
        let devicePropsRes = http.get(`${BASE_URL}${API_PREFIX}/device/1/properties`, { headers });
        check(devicePropsRes, {
            'device properties status is 200': (r) => r.status === 200,
        });

        sleep(0.5);
    });

    // Scenario 3: Alarm Monitoring
    group('Alarm Monitoring', function () {
        // Get active alarms
        let alarmRes = http.get(`${BASE_URL}${API_PREFIX}/alarms?status=active&pageNo=1&pageSize=50`, { headers });
        alarmListTime.add(alarmRes.timings.duration);

        check(alarmRes, {
            'alarm list status is 200': (r) => r.status === 200,
            'alarm list response time < 1s': (r) => r.timings.duration < 1000,
        });

        sleep(0.5);
    });

    // Scenario 4: Tag Values (simulating real-time updates)
    group('Real-time Tag Updates', function () {
        // Get tag values (batch)
        const tagIds = ['pump1_status', 'pump2_status', 'pressure_inlet', 'temp_outlet'];
        let tagRes = http.post(
            `${BASE_URL}${API_PREFIX}/tags/values`,
            JSON.stringify({ tagIds }),
            { headers }
        );

        check(tagRes, {
            'tag values status is 200': (r) => r.status === 200,
            'tag values response time < 500ms': (r) => r.timings.duration < 500,
        });

        sleep(0.2);
    });

    // Scenario 5: Control Commands (lower frequency)
    if (Math.random() < 0.1) { // 10% chance to send control command
        group('Control Commands', function () {
            const controlPayload = {
                deviceId: 1,
                commandName: 'start',
                commandType: 'SYNC',
                params: {}
            };

            let controlRes = http.post(
                `${BASE_URL}${API_PREFIX}/control`,
                JSON.stringify(controlPayload),
                { headers }
            );
            controlCommandTime.add(controlRes.timings.duration);

            check(controlRes, {
                'control command status is 200 or 202': (r) => r.status === 200 || r.status === 202,
            });

            sleep(1);
        });
    }

    // Random sleep between iterations
    sleep(Math.random() * 2 + 1);
}

// ==========================================
// Setup and Teardown
// ==========================================

export function setup() {
    console.log('Starting SCADA load test...');
    console.log(`Base URL: ${BASE_URL}`);

    // Verify system is accessible
    let healthCheck = http.get(`${BASE_URL}/actuator/health`);
    if (healthCheck.status !== 200) {
        console.warn('Health check failed, system may not be ready');
    }

    return { startTime: new Date().toISOString() };
}

export function teardown(data) {
    console.log('Load test completed');
    console.log(`Started at: ${data.startTime}`);
    console.log(`Ended at: ${new Date().toISOString()}`);
}

// ==========================================
// Summary Handler
// ==========================================

export function handleSummary(data) {
    const summary = {
        testRun: {
            timestamp: new Date().toISOString(),
            duration: data.state.testRunDurationMs,
            vus: data.metrics.vus ? data.metrics.vus.max : 0,
        },
        metrics: {
            http_reqs: data.metrics.http_reqs ? data.metrics.http_reqs.count : 0,
            http_req_duration_avg: data.metrics.http_req_duration ? data.metrics.http_req_duration.avg : 0,
            http_req_duration_p95: data.metrics.http_req_duration ? data.metrics.http_req_duration['p(95)'] : 0,
            http_req_failed: data.metrics.http_req_failed ? data.metrics.http_req_failed.rate : 0,
        },
        custom: {
            dashboard_load_p95: data.metrics.dashboard_load_time ? data.metrics.dashboard_load_time['p(95)'] : 0,
            device_list_p95: data.metrics.device_list_time ? data.metrics.device_list_time['p(95)'] : 0,
            alarm_list_p95: data.metrics.alarm_list_time ? data.metrics.alarm_list_time['p(95)'] : 0,
        },
        thresholds: data.thresholds,
    };

    return {
        'stdout': JSON.stringify(summary, null, 2) + '\n',
        './tests/scada/load-test-results.json': JSON.stringify(summary, null, 2),
    };
}
