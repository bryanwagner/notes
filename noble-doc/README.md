# Noble System Documentation

The stack used by Noble systems is:

* [Vue.js](https://vuejs.org/)
* [Express.js](https://expressjs.com/)
* [PostgreSQL](https://www.postgresql.org/)
* [Bitbucket Pipelines](https://bitbucket.org/product/features/pipelines)
* [AWS Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/)
* [AWS S3 static sites](https://docs.aws.amazon.com/AmazonS3/latest/userguide/WebsiteHosting.html)

## Repositories

### noble-express-api

Contains back-end services to manage case and call-center functionality using the [Vue.js](https://vuejs.org/) framework, [Express.js](https://expressjs.com/) to serve HTTP endpoints, and [PostgreSQL](https://www.postgresql.org/) for persistence. It is deployed as a [Docker](https://www.docker.com/) container to [Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/).

We use [Sequelize](https://sequelize.org/v3/) for ORM and database migrations.

Case and call-center management depends heavily on the [Twilio API](https://www.twilio.com/docs/usage/api). Phone numbers for customers and voicemail use [Twilio Webhooks](https://www.twilio.com/docs/usage/webhooks/getting-started-twilio-webhooks) to invoke our HTTP endpoints. For local development, you should [use ngrok to route Twilio webhooks to your local device](https://www.twilio.com/blog/2013/10/test-your-webhooks-locally-with-ngrok.html). You will need to set `API_URL` in your `.env` (see [below](#Configuration)).

The most important callback scripts for handling Twilio calls are in `src/middlewares/integration/twilio-voice.js` and `src/integrations/twilio-voice-call/events/connect.js`

We also integrate with [Slack](https://www.npmjs.com/package/slack) for chat management.

Running locally:

```
$ npm install
$ npm run start-dev
```

You can also run locally with [PM2](https://www.npmjs.com/package/pm2) (see [below](#Backend-servers)):

```
$ npm run start-local
```

But there seems to be an issue with stopping the server on Windows. To kill the servers under PM2 on Windows you can use TaskKill:

```
> netstat -a -n -o | grep :3000
#     Find this:
# TCP    0.0.0.0:3000    0.0.0.0:0    LISTENING    26516
> TaskKill.exe /F /PID 26516
```

### noble-web-console

*Notice: current development and release is being done on branch `redesign` which does not share a common root with `master`. We will need to merge these together to avoid confusion; for now use branch `redesign`.*

Static website using Vue.js that contains the case and call-center frontend. It is deployed to [AWS S3 as a static website](https://docs.aws.amazon.com/AmazonS3/latest/userguide/WebsiteHosting.html).

It communicates to the `noble-express-api` backend using [Axios](https://www.npmjs.com/package/axios) for HTTP requests. WebSockets are used for asynchronous events using [Socket.io](https://socket.io/).

Running locally:

```
$ npm install
$ npm run dev
```

### noble-client-site

Static website using Vue.js that contains the public landing site. It is deployed to [AWS S3 as a static website](https://docs.aws.amazon.com/AmazonS3/latest/userguide/WebsiteHosting.html).

## Configuration

We use [dotenv](https://www.npmjs.com/package/dotenv) for system configuration. For local development, use a `.env` file in your project root (which is ignored by `.gitignore`). See the `.env.example` files for a template, but note that these templates are out-of-date.

Local `.env` example template for `noble-express-api`:

```
#API_URL=<API_URL>
API_URL=https://44e395b3a59e.ngrok.io

PORT=3000
ENV=local

DB_USER=noble
DB_PASSWORD=<DB_PASSWORD>
DB_HOST=<DB_HOST>
DB_PORT=5432
DB_NAME=noble_staging

# todo: remove personal access keys (these should load from ~/.aws)
AWS_ACCESS_KEY=<AWS_ACCESS_KEY>
AWS_SECRET_ACCESS_KEY=<AWS_SECRET_ACCESS_KEY>
AWS_REGION=us-east-1
AWS_S3_BUCKET=noble-uploads-staging

CLIENT_NAVIGATOR_ID=1
NAVIGATOR_DEFAULT_TEAM=Team A
NAVIGATOR_PICKED_UP_TIMEOUT=20000
VOICEMAIL_REDIRECT_DELAY=0
NAVIGATOR_ROLE_NAME=navigator
RESTORE_NAVIGATOR_TIMEOUT=3000

LOG_NAME="noble-express-api-staging"
LOGGING_LEVEL=debug
MAX_FILE_SIZE=10485760

#SENTRY_KEY=<SENTRY_KEY>

JWT_KEY=<JWT_KEY>

SLACK_APP_ID=<SLACK_APP_ID>
SLACK_BOT_TOKEN=<SLACK_BOT_TOKEN>
SLACK_CLIENT_ID=<SLACK_CLIENT_ID>
SLACK_CLIENT_SECRET=<SLACK_CLIENT_SECRET>
TEAM_A_CHANNEL=<TEAM_A_CHANNEL>

STRIPE_SECRET_KEY=<STRIPE_SECRET_KEY>

TWILIO_ACCOUNT_SID=<TWILIO_ACCOUNT_SID>
TWILIO_API_KEY=<TWILIO_API_KEY>
TWILIO_API_SECRET=<TWILIO_API_SECRET>
TWILIO_AUTH_TOKEN=<TWILIO_AUTH_TOKEN>
TWILIO_PHONE_NUMBER=<TWILIO_PHONE_NUMBER>
TWILIO_VOICE_MAIL_NUMBER=<TWILIO_VOICE_MAIL_NUMBER>
TWIML_APP_SID=<TWIML_APP_SID>
VOICE_MAIL_RECORD_URL=<VOICE_MAIL_RECORD_URL>
```

Note: we should not be storing AWS credentials in the `.env` file. We should use the [AWS Shared Credentials File](https://docs.aws.amazon.com/sdk-for-javascript/v2/developer-guide/loading-node-credentials-shared.html) in your local environment:

In hosted environments on AWS (production, staging, and develop), the env variables are located within the Elastic Beanstalk configuration for each environment. For example, in the AWS Console, select the correct region (`us-east-1`) and navigate to:

`AWS Console > Elastic Beanstalk > Environments > noble-express-api-develop > Configuration > Category: Software > Edit`

## Deployment

### CI/CD

The CI/CD pipeline we use is [Bitbucket Pipelines](https://bitbucket.org/product/features/pipelines). Each project contains a `bitbucket-pipelines.yml` file in the repository root which configures the pipeline.

Bitbucket Pipelines is configured to deploy to a target environment when a tag is created. The actual tag strings are scanned for prefixes matching the children of `pipelines: tags:` in the configuration. For example, to deploy to the develop environment:

```
# commit and push changes
$ git add <updated files>
$ git commit -m "[JIRA-###] Your message describing changes."
$ git push

# find the latest tag
$ git tag -l "develop-*"

# for example, the latest tag is "develop-v-107"
$ git tag "develop-v-108"
$ git push --tags
```

Pushing a new tag triggers Bitbucket Pipelines to begin a CI/CD pipeline which you can view at:

`bitbucket.org > noble-express-api > Pipelines`

To deploy to AWS, we use a publicly available pipe named `atlassian/aws-elasticbeanstalk-deploy` which is itself [hosted in a Bitbucket repository](https://bitbucket.org/atlassian/aws-elasticbeanstalk-deploy), which manages the build of a Docker container, the upload of that container to S3, and the restart of the server in AWS Beanstalk. [More info here](https://support.atlassian.com/bitbucket-cloud/docs/deploy-to-aws-with-elastic-beanstalk/).

Our current development process is simply to commit to master and tag, but in the future, we recommend using [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

There is an IAM identity named `ci-cd-user` that is used for CI/CD into AWS.

### DNS and Hosting

Domain names are mapped in [Route 53](https://aws.amazon.com/route53/):

`AWS Console > Route 53 > Hosted zones`

We use [CloudFront](https://aws.amazon.com/cloudfront/) for CDN edge-caching:

`AWS Console > CloudFront > Distributions`

### Backend servers

`noble-express-api` and possibly other future backend services are managed as [Docker](https://www.docker.com/) containers in [Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/).

Load balancing is configured with [PM2](https://www.npmjs.com/package/pm2) which launches Node.js in [cluster mode](https://pm2.keymetrics.io/docs/usage/cluster-mode/). See `pm2.js` in the project root.

Backend servers can be restarted from Elastic Beanstalk:

`AWS Console > Elastic Beanstalk > Environments > noble-express-api-production > Actions > Restart app servers(s)`

Note that the static websites do not need to be restarted as they are simply hosted statically by S3.

### Logging

Logs are captured and aggregated to [CloudWatch](https://aws.amazon.com/cloudwatch/) using [winston-cloudwatch](https://www.npmjs.com/package/winston-cloudwatch). You can find the logs at:

`AWS Console > Cloudwatch > Logs > Log groups > noble-express-api-production > Log streams`

More info about [winston configuration here](https://www.npmjs.com/package/winston).

To do: install [Logging Middleware](https://medium.com/@selvaganesh93/how-node-js-middleware-works-d8e02a936113) for HTTP endpoints (see Example 2).

### Database

We persist backend state in a PostgreSQL database hosted in [RDS](https://aws.amazon.com/rds/).

### DDOS

We are using the [ddos](https://www.npmjs.com/package/ddos) library to protect against Denial-of-Service attacks.

Originally, the configuration was too aggressive for the amount of requests normally sent by the frontend, which was causing frontend errors, so we relaxed its configuration. If you see ddos errors in the logs, we may need to relax the limits further. See the `DDOS_BURST` and `DDOS_LIMIT` env variables for configuration.
