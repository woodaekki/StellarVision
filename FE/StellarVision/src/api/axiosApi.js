import axios from "axios";


const api = axios.create({
baseURL: 'https://i13c106.p.ssafy.io/api',
timeout: 5000,
withCredentials: true,
headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(
function (config) {
const token = localStorage.getItem('jwt');
if (token) {
config.headers.Authorization = 'Bearer ' + token;
}
return config;
},
function (error) {
return Promise.reject(error);
}
);

export default api;
