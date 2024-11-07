import FormHandler from "./js/FormHandler.js";
import render from "./js/render.js";
import Chat from "./js/Chat.js";
import FriendTab from "./js/FriendTab.js";
import Navigator from "./js/navigator.js";
import AccountTab from "./js/AccountTab.js";
import CreateGroupTab from "./js/CreateGroupTab.js";
import eventBus from "./js/eventBus.js";

const app = document.querySelector('#app')
const startEvent = new CustomEvent('startEvent')

function getSubFromJwt(token) {
    const parts = token.split('.');

    if (parts.length !== 3) {
        throw new Error('Invalid JWT token');
    }

    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));

    return payload.sub || null;
}

function getJwtFromCookie() {
    const cookies = document.cookie.split('; ');

    for (const cookie of cookies) {
        const [key, value] = cookie.split('=');

        if (key === 'my-chat-app-jwt') {
            return value;
        }
    }

    return null;
}

async function login() {
    new FormHandler(app, startEvent)
}

async function start(){
    const JWT = getJwtFromCookie()
    
    if(!JWT){
        return login()
    }

    const connected = await fetch('http://localhost:8080/ping', {
        method: 'GET',
        headers: {'Authorization': `Bearer ${JWT}`,},
        credentials: 'include'
    }).then(response => response.status === 200)

    console.log(connected);
    
    
    if(!connected){
        return login()
    }

    await render(app)

    const socket = new SockJS(`http://localhost:8080/ws?token=${JWT}`)
    const stompClient = Stomp.over(socket); 
    stompClient.connect({ Authorization: `Bearer ${JWT}` }, frame => {
        const sub = getSubFromJwt(JWT)

        stompClient.subscribe(`/global-notification/${sub}`, (message) => {
            const msg = JSON.parse(message.body)
            
            if(msg.type === 'RELOAD') location.reload()

        })

        new Chat(app, JWT, sub, stompClient)
        new FriendTab(app, JWT, sub, stompClient)
        new Navigator(app)
        new AccountTab(app, JWT, sub, stompClient)
        new CreateGroupTab(app, JWT, sub, stompClient)
    }, function (error) {
        console.error('Error connecting to WebSocket:', error);
    });   
}

async function dispatch() {
    app.addEventListener('startEvent', start)
    app.dispatchEvent(startEvent)
}

dispatch()

