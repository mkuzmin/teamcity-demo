FROM nginx:1.11.5-alpine
MAINTAINER Michael Kuzmin "michael.kuzmin@jetbrains.com"
COPY ./index.html /usr/share/nginx/html/
COPY ./app/ /usr/share/nginx/html/app/
COPY ./libs/ /usr/share/nginx/html/libs/
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
#HEALTHCHECK --timeout=3s --interval=5s --retries=10 CMD wget -q http://localhost  -O /dev/null
