.PHONY: dev backend frontend install clean

# ── One-command dev start ─────────────────────────────────────────────────────
dev: install
	@echo "Starting backend and frontend..."
	@(cd backend && mvn spring-boot:run) & \
	 (cd frontend && npm start) & \
	 wait

# ── Individual targets ────────────────────────────────────────────────────────
backend:
	cd backend && mvn spring-boot:run

frontend:
	cd frontend && npm start

install:
	@if [ ! -d "frontend/node_modules" ]; then \
		echo "Installing frontend dependencies..."; \
		cd frontend && npm install; \
	fi

# ── Build production artifacts ────────────────────────────────────────────────
build:
	cd backend && mvn clean package -DskipTests
	cd frontend && npm run build

# ── Tests ─────────────────────────────────────────────────────────────────────
test:
	cd backend && mvn test

# ── Clean build artifacts ─────────────────────────────────────────────────────
clean:
	cd backend && mvn clean
	rm -rf frontend/build
	@echo "Cleaned build artifacts. H2 data preserved."

# ── Reset database (re-seeds on next boot) ────────────────────────────────────
reset-db:
	rm -f backend/data/moviedb.mv.db backend/data/moviedb.trace.db
	@echo "Database reset. Run 'make backend' to re-seed."
