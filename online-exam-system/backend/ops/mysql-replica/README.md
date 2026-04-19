# MySQL Replica

This directory contains the local single-host MySQL replica setup used for phase 5 verification.

- Primary: `127.0.0.1:3306`
- Replica: `127.0.0.1:3307`
- Runtime data: `backend/ops/mysql-replica/runtime/`

Use [setup-mysql-replica.ps1](/D:/test/online-exam-system/backend/ops/scripts/setup-mysql-replica.ps1) to initialize or rebuild the replica.
Use [stop-mysql-replica.ps1](/D:/test/online-exam-system/backend/ops/scripts/stop-mysql-replica.ps1) to stop it.
