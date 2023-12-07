package database

import (
	"context"

	"github.com/cockroachdb/errors"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"go.mongodb.org/mongo-driver/x/mongo/driver/connstring"
)

type MongoClient struct {
	*mongo.Client
	Config       *MongoClientConfig
	DatabaseName string
}

type MongoClientConfig struct {
	URI string `env:"URI,required"`
}

func OpenMongoClient(ctx context.Context, config *MongoClientConfig) (*MongoClient, error) {
	cs, err := connstring.Parse(config.URI)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	if cs.Database == "" {
		return nil, errors.New("database name is required")
	}

	opts := options.Client()
	opts.ApplyURI(config.URI)
	if err := opts.Validate(); err != nil {
		return nil, errors.WithStack(err)
	}

	client, err := mongo.Connect(ctx, opts)
	if err != nil {
		return nil, errors.WithStack(err)
	}

	return &MongoClient{
		Client:       client,
		Config:       config,
		DatabaseName: cs.Database,
	}, nil
}
