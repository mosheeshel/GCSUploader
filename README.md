# GCS Uploader - Android File Sharing to Google Cloud Storage

## Overview

GCS Uploader is an Android application that allows users to quickly share files directly to Google Cloud Storage using their Google Account. With a simple interface, you can:

- Authenticate using Google OAuth
- Select from buckets you have access to
- Upload files via Android's share action
- Remember your last used bucket

## Features

- 🔒 Secure Google Account Authentication
- 📁 Dynamic Bucket Selection
- 💾 File Upload via Share Intent
- 🔄 Persistent Bucket Preference

## Prerequisites

Before using the app, you'll need:

- Android device running Android 8.0 (API level 26) or higher
- Google Cloud Platform account
- Configured Google Cloud Storage buckets

## Setup Instructions

### 1. Google Cloud Project Configuration

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the following APIs:
    - Google Cloud Storage API
    - Google Drive API

### 2. OAuth 2.0 Credentials

1. Navigate to "Credentials" in Google Cloud Console
2. Click "Create Credentials" > "OAuth client ID"
3. Select "Android" as the application type
4. Generate a package-signed certificate
5. Download and add the `google-services.json`

### 3. Bucket Permissions

Ensure your Google account has the following IAM roles on your buckets:
- Storage Object Viewer
- Storage Object Creator

## Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Replace `"your-project-id"` in `MainActivity.kt`
5. Add your `google-services.json`

## Usage

1. Open the app
2. Authenticate with your Google Account
3. Select a bucket
4. Use Android's share action on any file
5. The file will upload to the selected bucket

## Permissions

The app requires:
- Internet access
- Google account permissions
- File read access

## Limitations

- Currently supports file uploads only
- Requires manual bucket selection
- Minimal error handling

## Contributing

Contributions are welcome! Please:
- Fork the repository
- Create a feature branch
- Submit a pull request

## License

[Specify your license - e.g., MIT, Apache 2.0]

## Troubleshooting

- Ensure Google Cloud APIs are enabled
- Check internet connectivity
- Verify bucket permissions
- Regenerate OAuth credentials if needed

## Contact

For issues or support, please [add your contact method or open a GitHub issue]

## Disclaimer

This is an experimental project. Use in production environments with caution.