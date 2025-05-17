# Setting Up Google OAuth with Supabase

This guide explains how to configure Google OAuth authentication with Supabase for the ACL Booking System.

## Supabase Configuration

1. **Log in to your Supabase Dashboard**:
   - Navigate to your Supabase project dashboard at https://app.supabase.com

2. **Enable Google OAuth**:
   - In the left sidebar, click on "Authentication"
   - Navigate to "Providers" tab
   - Find "Google" in the list of providers and toggle it ON

3. **Configure Google OAuth Credentials**:
   - You'll need to provide:
     - Client ID: Get this from Google Cloud Console
     - Client Secret: Get this from Google Cloud Console

4. **Add Redirect URLs**:
   - Add the following redirect URL to your Supabase configuration:
     ```
     http://localhost:8081/api/oauth/callback
     ```
   - For production, add your production URL as well.

## Google Cloud Console Setup

1. **Create a new project** in Google Cloud Console:
   - Go to https://console.cloud.google.com/
   - Click "Select a project" at the top right, then "NEW PROJECT"
   - Enter a name and click "CREATE"

2. **Configure OAuth Consent Screen**:
   - In the sidebar, go to "APIs & Services" > "OAuth consent screen"
   - Select "External" user type (unless you're using Google Workspace)
   - Fill in the required fields:
     - App name: "ACL Booking System"
     - User support email: Your email
     - Authorized domains: Add your domain
     - Developer contact information: Your email
   - Click "SAVE AND CONTINUE"
   - Add scopes: email, profile, openid (minimum required)
   - Click "SAVE AND CONTINUE"
   - Add test users if needed
   - Click "SAVE AND CONTINUE"

3. **Create OAuth 2.0 Credentials**:
   - In the sidebar, go to "APIs & Services" > "Credentials"
   - Click "CREATE CREDENTIALS" > "OAuth client ID"
   - Choose "Web application" as application type
   - Name: "ACL Booking System Web Client"
   - Add Authorized JavaScript origins:
     ```
     http://localhost:8081
     ```
   - Add Authorized redirect URIs:
     ```
     http://localhost:8081/api/oauth/callback
     ```
     (This MUST match what you configured in Supabase)
   - Click "CREATE"
   - Note your Client ID and Client Secret

4. **Add these credentials to Supabase** as described in the previous section.

## Testing the Setup

1. Navigate to the Google Login page:
   ```
   http://localhost:8081/google-login
   ```

2. Click the "Sign in with Google" button

3. If everything is configured correctly:
   - You'll be redirected to Google's authentication page
   - After authenticating, you'll be redirected back to our app
   - The app will store your authentication token in localStorage
   - You'll be able to access protected resources

## Troubleshooting

- **Redirect URI Mismatch**: Ensure the redirect URIs in Google Console exactly match those in Supabase
- **CORS Issues**: If encountering CORS errors, verify the Authorized JavaScript origins in Google Console
- **Supabase Configuration**: Double-check that Google provider is enabled in Supabase
- **Network Issues**: Check developer console for network errors during the OAuth flow

## Next Steps

1. Consider implementing proper token refresh logic
2. Add user profile data fetching from Supabase
3. Implement logout functionality
4. Add access control based on user roles 