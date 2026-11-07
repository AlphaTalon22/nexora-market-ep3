export const environment = {
  production: true,
  apiBaseUrl: 'https://REEMPLAZAR.execute-api.us-east-1.amazonaws.com',
  auth: {
    clientId: 'f8d02b1a-9d14-4d7e-87c4-24bbf430721a',
    authority: 'https://login.microsoftonline.com/5f26903b-8f3c-4181-afbb-a40c0b6e034d',
    redirectUri: 'https://REEMPLAZAR.cloudfront.net',
    postLogoutRedirectUri: 'https://REEMPLAZAR.cloudfront.net',
    scopes: ['api://967df881-c709-4c72-9c7c-787c1aa5557d/access_as_user'],
  },
};
