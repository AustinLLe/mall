FROM nginx:1.28-alpine

COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf

# HBuilderX 发行目录可能是 h5/ 或 web/；由 compose 的 H5_DIST_DIR 传入。
ARG H5_DIST_DIR=shopping_front/dist/build/h5
COPY ${H5_DIST_DIR}/ /usr/share/nginx/html/

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
  CMD wget -q -O - http://127.0.0.1/ >/dev/null || exit 1
