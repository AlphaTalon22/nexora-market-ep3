import { PublicClientApplication } from '@azure/msal-browser';
import { environment } from '../../../environments/environment';

export const msalInstance = new PublicClientApplication({
  auth: {
    clientId: environment.auth.clientId,
    authority: environment.auth.authority,
    redirectUri: environment.auth.redirectUri,
    postLogoutRedirectUri: environment.auth.postLogoutRedirectUri,
  },
});
