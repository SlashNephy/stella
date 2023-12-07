start:
	go run ./cmd/server

dev:
	go run github.com/cosmtrek/air

build:
	go build ./cmd/server

generate:
	go generate ./...

test: generate
	go test -v ./...

migrate:
	go install -tags 'mongodb' github.com/golang-migrate/migrate/v4/cmd/migrate@latest
	migrate -path migrations -database "mongodb://root:password@localhost:27017/stella?authSource=admin" up
