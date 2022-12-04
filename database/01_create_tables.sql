CREATE TABLE photos (
    photo_id varchar(64) NOT NULL,
    photo_slug varchar(100) UNIQUE NOT NULL,

    uri varchar(512),
    width integer,
    height integer,

    version integer NOT NULL,
    archived boolean,
    created TIMESTAMP WITH TIME ZONE NOT NULL,
    updated TIMESTAMP WITH TIME ZONE,

    model_id varchar(128),
    PRIMARY KEY (photo_id),
    CONSTRAINT fk_models
        FOREIGN KEY(model_id)
            REFERENCES models(model_id)
);

CREATE TABLE models (
    model_id varchar(64) NOT NULL,
    model_slug varchar(100) UNIQUE NOT NULL,

    given_name varchar(64) NOT NULL,
    family_name varchar(64) NOT NULL,
    eye_color varchar(32),
    height integer,

    version integer,
    archived boolean,
    created TIMESTAMP WITH TIME ZONE NOT NULL,
    updated TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (model_id)
)

/*
 Open questions:
 - Should we model the relationship between models and photos how. Let's add an additional table, much more flexibility. No.

 - Do we want categories at this point? What's the point. Yes -> It'll show the complexity of the code better.
 - Do we want to have works at this point? What's the point -> Yes -> it's meant to replace the current website.
 - Do we want to draw DynamoDB?
 */