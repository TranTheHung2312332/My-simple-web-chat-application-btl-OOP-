export default class FormHandler {
    constructor(app, startEvent){
        this.app = app
        this.startEvent = startEvent
        this.renderForm()
    }

    changeToRegister(e){
        e.preventDefault()
        this.loginForm.classList.add('hide')
        this.registerForm.classList.remove('hide')
    }

    changeToLogin(e){
        e.preventDefault()
        this.loginForm.classList.remove('hide')
        this.registerForm.classList.add('hide')
    }

    login(body){
        fetch('http://localhost:8080/public/login', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        })
        .then(response => response.json())
        .then(response => {
            if(response.success)
                return response.data
            throw new Error(response.error)
        })
        .then(data => {
            const token = data.token
            this.setCookie('my-chat-app-jwt', token, 30)
            this.destructor()
        })
        .catch(e => {
            this.errorMsgUls_login[0].innerHTML += `<li>${e.message}</li>`
            this.errorMsgUls_login[1].innerHTML += `<li>${e.message}</li>`
        })
    }

    handleLogin(e){
        e.preventDefault()
        const email = this.loginForm.querySelector('#email').value
        const rawPassword = this.loginForm.querySelector('#password').value

        if(!email || !password)
            return

        for(const ul of this.errorMsgUls_login)
            ul.innerHTML = ""

        const body = {email, rawPassword}
        this.login(body)
    }

    handleRegister(e){
        e.preventDefault()
        const username = this.registerForm.querySelector('#username-register').value.trim()
        const email = this.registerForm.querySelector('#email-register').value.trim()
        const password = this.registerForm.querySelector('#password-register').value.trim()

        for(const ul of this.errorMsgUls_register)
            ul.innerHTML = ""
        
        const body = {username, email, password}
        fetch('http://localhost:8080/public/register', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        })
        .then(response => response.json())
        .then(response => {            
            if(response.success)
                return {email, rawPassword: password}
            throw new Error(JSON.stringify(response.error))
        })
        .then(loginData => this.login(loginData))
        .catch(e => {
            const errorField = JSON.parse(e.message)
            if(errorField instanceof Array){
                errorField.forEach(error => {
                    if(error.startsWith('username: '))
                        this.errorMsgUls_register[0].innerHTML += `<li>${error.slice(10)}</li>`
                    else if(error.startsWith('email: '))
                        this.errorMsgUls_register[1].innerHTML += `<li>${error.slice(7)}</li>`
                    else
                        this.errorMsgUls_register[2].innerHTML += `<li>${error.slice(10)}</li>`
                })
            }
            else if(errorField === 'EMAIL_EXISTED')
                this.errorMsgUls_register[1].innerHTML += `<li>This email has been used</li>`
        })
    }

    setCookie(name, value, days){
        const expires = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString();
        document.cookie = `${name}=${value}; expires=${expires}; path=/; secure; SameSite=Strict`;
    };

    async renderForm(){
        await fetch('./components/register.html')
        .then(response => response.text())
        .then(html => this.app.innerHTML += html)

        await fetch('./components/login.html')
        .then(response => response.text())
        .then(html => this.app.innerHTML += html)

        this.loginForm = this.app.querySelector('#login-form')
        this.registerForm = this.app.querySelector('#register-form')
        this.loginForm.classList.remove('hide')   
             

        this.loginForm.querySelector('a').onclick = e => this.changeToRegister(e)
        this.registerForm.querySelector('a').onclick = e => this.changeToLogin(e)
        
        this.loginForm.querySelector('button[type="submit"]').onclick = e => this.handleLogin(e)
        this.registerForm.querySelector('button[type="submit"]').onclick = e => this.handleRegister(e)

        this.errorMsgUls_register = this.registerForm.querySelectorAll('.invalid-field-msg')
        this.errorMsgUls_login = this.loginForm.querySelectorAll('.invalid-field-msg')
    }

    destructor(){
        this.loginForm.remove()
        this.registerForm.remove()
        this.app.dispatchEvent(this.startEvent)
    }
}