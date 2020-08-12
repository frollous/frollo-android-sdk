## Changelog

### 3.6.0

#### Changes

- Optimize Transactions advanced filtering and searching
- Add Account Features & CDR Product Information to Account API Response
- Migrate from Application Context to ContentProvider Context

### 3.5.0

#### Features

- Transactions advanced filtering and searching

#### Changes

- Transaction pagination improvements

### 3.4.0

#### Features

- Generic metadata support and additional properties for surveys
- Refactor and Remodel Transaction Reports
- Merchants Pagination
- Budgets & Budget Periods

#### Changes

- Update Frollo Host API version to 2.7
- Add properties - 'aggregator_type' & 'permissions' to Provider model

### 3.3.0

#### Features

- Force a refresh of a provider account with the aggregator
- Refresh cached merchant data
- Goals enhancements and generic metadata support
- Messages generic metadata support

#### Changes

- Update Frollo Host API version to 2.6
- Send app version in addition to SDK version in headers
- Messages supports expanded URL opening methods

### 3.2.0

#### Changes
- Added support to show reports based on tag.

### 3.1.0

#### Changes
- Added support to change a transactions budget category and to apply it to all similar transactions

### 3.0.0

#### Changes
- Improved custom authentication making it easier to implement
- Add externalId for mapping external aggregator ID for ProviderAccounts, Accounts & Transactions
- Allow merchant image to be optional

### 2.1.0

#### Changes
- Goals
- Handle requests on token expiry and cancel queued requests on reset
- Add method for initializing authentication callbacks for custom authentication

#### Features
- Goals
- Update Frollo Host API version to 2.4

### 2.0.5

#### Changes
- Update Account - Update cache before firing the network request

### 2.0.4

#### Changes
- Bug Fix - Do not delete DISABLED & UNSUPPORTED providers from database while saving response from refresh providers API

### 2.0.3

#### Changes
- Bug Fix - Revert back to use Frollo Host API v2.2. Will update to Frollo Host API v2.4 in a future version.

### 2.0.2

#### Changes
- Add Client ID to User Register & User (Password) Reset API requests

### 2.0.1

#### Changes
- Token Injection (cherrypicked from 1.3.4)
- Bug Fix - Token Expiry Calculation (cherrypicked from 1.3.4)

### 2.0.0
- Custom Authentication - Custom handling of authentication can be provided to the SDK

#### Features
- Custom Authentication

### 1.4.4

#### Changes
- Bug Fix - Revert back to use Frollo Host API v2.2. Will update to Frollo Host API v2.4 in a future version.

### 1.4.3

#### Changes
- Token Injection (cherrypicked from 1.3.4)
- Bug Fix - Token Expiry Calculation (cherrypicked from 1.3.4)

### 1.4.2

#### Changes
- Update Frollo Host API version to 2.4 to support the refactored Account Balance Reports API.

### 1.4.1

#### Changes
- Refactor Aggregation - createProviderAccount to return the ID of the provider account created.

### 1.4.0

#### Changes
- Update Tagging APIs & methods
- Enhanced methods for Aggregation, Reports, Surveys, Messages
- Improved handling of logged out status

### 1.3.4

#### Changes
- Token Injection
- Bug Fix - Token Expiry Calculation

### 1.3.3

#### Changes
- Logout calls OAuth2 revoke token API

### 1.3.2

#### Changes
- Migrate user to new identity provider

### 1.3.1

#### Changes
- Allow custom parameters in authorization code flow

### 1.3.0
- Bills
- Enhanced Transaction - with Merchant location & phone
- Support for fetching latest published survey
- Auto dismiss flag for Messages
- Transaction tags

#### Features
- Bills
- Bill Payments
- Enriched merchant details on a Transaction

### 1.2.0
- Reports

#### Features
- Account Balance Reports
- Current Transaction Reports
- History Transaction Reports

### 1.1.0
- Surveys

#### Features
- Surveys

### 1.0.0
- Initial release

#### Features
- OAuth2 Authentication
- Aggregation
- Events
- Messages
- Notifications
- User