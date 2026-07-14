/** @type {import('next').NextConfig} */
const nextConfig = {
  basePath: process.env.SAGEMAKER === '1'
    ? '/codeeditor/default/absports/3000'
    : '',
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
