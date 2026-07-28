FROM nginx:1.28-alpine

COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY shopping_front/unpackage/dist/build/h5/ /usr/share/nginx/html/

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
  CMD wget -q -O - http://127.0.0.1/ >/dev/null || exit 1
