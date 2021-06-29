## Overview

### Introduction

The Frollo Android SDK is designed to simplify integration of the Frollo APIs by providing all the tools needed to manage caching of data from the APIs and authentication. Linking of data between APIs and retry of requests when authentication has expired is all managed automatically.

The Frollo Android SDK is broken down into multiple components that reflect the features on our APIs. At a high level the features are broken down as follows:

* [Authentication](#authentication) - Login of the user and refreshing of access tokens to use the APIs
* [User](#user) - Manages the user profile and creating user accounts (if supported)
* [Aggregation](#aggregation) - Everything to do with accounts and transactions is managed here including categorisation, tagging, merchants and linking accounts. Accounts can be aggregated from direct integration or aggregation partners
* [Bills](#bills) - Tracking and detection of bills from the user's aggregated accounts
* [Budget and Pay Day](#budget-and-pay-day) - Tracking of budgets against merchants, categories and buckets against a user's pay cycle
* [Reports](#reports) - Reporting of spend (against budget if available) broken down into merchants, categories and buckets in various periods
* [Goals](#goals) - Savings and debt repayment goals with automatic tracking of payments
* [Events](#events) - Triggering of events and sequences from user actions
* [Messages](#messages) - Nudges and other customisable messaging to inform the user and keep them on track
* [Surveys](#surveys) - Questions and feedback from the user
* [Payments](#payments) - Manages payments
* [Images](#images) - Upload images for budgets, goals etc..
* [Contacts](#contacts) - TBD
* [Cards](#cards) - TBD
* [KYC](#kyc) - TBD
* [ManagedProducts](#managedproducts) - TBD
* [Payday](#payday) - TBD
* [AddressManagement](#addressmanagement) - TBD

### Authentication

Authentication of the user is currently managed by use of OAuth2. Users can be authenticated by any OpenID Connect compliant identity provider or the Frollo identity provider.

Authentication is supported using the following OAuth2 flows:

* [Resource Owner Password Credential (ROPC)](https://auth0.com/docs/api-auth/grant/password)
* [Authorization Code with PKCE](https://auth0.com/docs/flows/concepts/auth-code-pkce)

Authentication provides the access token needed to access Frollo APIs and manages refreshing the access token if it expires.

See [OAuth2Authentication](us.frollo.frollosdk.authentication/-o-auth2-authentication/index.html) for more details.

### User

User management provides utilities to manage the user profile and device. The user profile can contain personal information about the user used to personalise the Frollo experience, for example providing location relevant recommendations and comparisons on spending.

The following are taken care of here:

* Updating the user profile - e.g. name, postcode etc
* Deleting the user from Frollo
* Updating the device information - e.g. timezone, device info, compliance status

The following are available depending on how authentication and users are configured for your tenant:

* Registering a user by email
* Changing the user's password
* Reset password

See [UserManagement](us.frollo.frollosdk.user/-user-management/index.html) for more details.

### Aggregation

Aggregation manages the user's financial and other accounts data. This gives an aggregate view of a user's bank accounts, loans, credit information, loyalty, insurance and other data. Linking and customisation of this data is also managed here.

Account data can come from many different sources including:

* Aggregation partners
* Open banking
* Direct integrations

An institution is referred to as a [Provider](us.frollo.frollosdk.model.coredata.aggregation.providers/-provider/index.html) and the login or connection to that provider as a [ProviderAccount](us.frollo.frollosdk.model.coredata.aggregation.provideraccounts/-provider-account/index.html). The actual account data is referred to as an [Account](us.frollo.frollosdk.model.coredata.aggregation.accounts/-account/index.html) as each provider account may have multiple accounts linked to it. An example would be:

* **Provider**: ANZ
   * **Provider Account**: user@example.com
      * **Account**: Bank Account
      * **Account**: Savings Account
      * **Account**: Credit Card

Supported account types include:

* Bank Accounts
* Credit Cards
* Loans
* Investments
* Insurance
* Rewards and Loyalty
* Credit / Financial Scores

Transactions can support tagging for personalisation at a granular level or allow for spending breakdown to be mesaured at higher level through automatic categorisation and allocation to "buckets". Buckets include income, living, lifestyle and savings. Naming of these buckets can be changed at the UI level and allocation between them can be determined by configuration of the host tenant. These are referred to as a [BudgetCategory](us.frollo.frollosdk.model.coredata.shared/-budget-category/index.html) in the API.

The following features are part of aggregation:

* Linking accounts
* Account information and enrichment
* Transaction information and enrichment
* Segregating spend into "buckets"
* Categorisation and tagging of transactions
* Merchant information
* Searching of transactions

Note: Aggregation refers the the aggregated view of accounts seen within Frollo, whether this includes accounts from external aggregation partners will depend on how your tenant is configured.

See [Aggregation](us.frollo.frollosdk.aggregation/-aggregation/index.html) for more details.

### Bills

Bills allows users to manage bills and track payments against them. Bill detection is automatic and users can then confirm they wish to track payments against these bills or a user can add manual bills if needed.

The following features are part of bills:

* Bill detection
* Bill payment tracking
* Payment due reminders
* Payments forecasting
* Manual bills

See [Bills](us.frollo.frollosdk.bills/-bills/index.html) for more details.

### Budget and Pay Day

Budgets and pay day allow the user to setup a budget based on their own pay cycle. Budgets can be setup to track spending against merchants, categories and buckets. Setting up budgets at the bucket level allows users to group spending together and easily track against proven budgeting methods (e.g. envelope or buckets).

The following features are part of budgets and pay day:

* Set pay day and pay period
* Set multiple budgets
* Set budgets by category, merchant or bucket

See [Budgets](us.frollo.frollosdk.budgets/-budgets/index.html) for more details.

### Payday

*Payday is Coming Soon to SDK*

### Reports

Reports give a breakdown of a user's spending or an account balance history. Reports can be used to show the user spending in the current month or allow the user to drill down and explore their spend in a variety of ways. Reports data is also suitable for driving graphs.

Reports can be generated across different time periods and broken down into several different ways including by category, by merchant and by bucket.

The following features are part of reports:

* Account balance history
* Current spend - track the spend for the current month against a budget
* Historical spend - breakdown the spend of a user for them to explore and find where money is going
* Tracking against a budget - if available a budget value will be returned to allow the user to visually see how they're tracking

See [Reports](us.frollo.frollosdk.reports/-reports/index.html) for more details.

### Goals

Goals help the user set savings or debt repayment goals and meet them. Goals allow the user to set a target amount and/or date and/or how much they can afford each month and their target date/repayment/total is calculated from this. Savings towards this goal are automatically tracked from deposits to an associated bank or savings account.

The following features are part of goals and challenges:

* Track credits, debits or credits and debits towards a goal's progress automatically
* Project goal progress and track if on course
* Save towards a date, amount or open-ended target
* See a breakdown of each period's progress towards a goal and calculate how to get back on track
 
See [Goals](us.frollo.frollosdk.goals/-goals/index.html) for more details.

### Events

Events allow user actions to drive changes or chains of actions on the Frollo host. For example a user could reach some fitness goals using HealthKit and trigger an event to increase their lifestyle budget for the next week as a reward.

See [Events](us.frollo.frollosdk.events/-events/index.html) for more details.

### Messages

Messages allow feedback to be provided to the user in the form of nudges and other content. Messages are a customisable content delivery system allowing the system to provide reminders back to the user to keep on track, drive offers or any manner of content the user may need.

The following features are supported as part of messages:

* Text, Video, Image and HTML content
* Tracking reading of and interaction with the message
* Driving the user to deep linked content in a consuming app
* Chaining and trumping of messages to ensure the user doesn't get bombarded with duplicates and only has relevant content at the right time

See [Messages](us.frollo.frollosdk.messages/-messages/index.html) for more details

### Surveys

Surveys allow you to collect feedback and conduct surveys from the user. This can be used to find how a user feels about their financial situation and drive events based on that or even just get feedback on the consuming application.

The following features are supported as part of surveys:

* Multiple questions
* Multiple choice answers
* Dynamic image based answers (choose an emoji that represents how you feel)
* Custom input (freeform) answers
* Triggering events based on answers

See [Surveys](us.frollo.frollosdk.surveys/-surveys/index.html) for more details

### Payments

Payments help the user to pay via Account Number & BSB, BPay, NPP or Transfer.

See [Payments](us.frollo.frollosdk.payments/-payments/index.html) for more details.

### Images

Images help the user to upload images for his budgets, goals etc..

See [Images](us.frollo.frollosdk.images/-images/index.html) for more details.

### Contacts

TBD

See [Contacts](us.frollo.frollosdk.contacts/-contacts/index.html) for more details.

### Cards

TBD

See [Cards](us.frollo.frollosdk.cards/-cards/index.html) for more details.

### KYC

TBD

See [KYC](us.frollo.frollosdk.kyc/-kyc/index.html) for more details.

### Managed Products

TBD

See [ManagedProducts](us.frollo.frollosdk.managedproducts/-managedproducts/index.html) for more details.

### Payday

TBD

See [Paydays](us.frollo.frollosdk.paydays/-paydays/index.html) for more details.

### AddressManagement

TBD

See [AddressManagement](us.frollo.frollosdk.address/-address-management/index.html) for more details.
