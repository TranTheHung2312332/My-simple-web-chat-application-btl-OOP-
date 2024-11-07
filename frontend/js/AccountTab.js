import Tab from "./Tab.js"

export default class AccountTab extends Tab {
    constructor(app, token, id, stomp){
        super()
        this.app = app
        this.token = token
        this.id = id
        this.stomp = stomp

        this.component = app.querySelector('#account-detail')
        this.img = app.querySelector('#account img')

        this.changePasswordButton = this.component.querySelector('#change-password')
        this.setAvatarButton = this.component.querySelector('#set-avatar')
        this.logoutButton = this.component.querySelector('#log-out')

        this.changePasswordModal = app.querySelector('#change-password-modal')
        this.submitChangeButton = this.changePasswordModal.querySelector('#change-password-submit')

        this.oldPasswordInput = this.changePasswordModal.querySelector('#old-password')
        this.newPasswordInput = this.changePasswordModal.querySelector('#new-password')

        this.invalidOldPasswordMsg = this.changePasswordModal.querySelector('#old-password-msg')        
        this.invalidNewPasswordMsg = this.changePasswordModal.querySelector('#new-password-msg')  
        
        this.loadInfo()
        
        this.addListener()
    }

    addListener(){
        this.img.onclick = e => this.component.classList.toggle('hide')
        
        this.logoutButton.onclick = e => {
            document.cookie = 'my-chat-app-jwt=; Max-Age=0; path=/;'
            location.reload()
        }

        this.changePasswordButton.onclick = e => {
            this.changePasswordModal.classList.remove('hide')
        }

        this.setAvatarButton.onclick = e => {
            initializeUploadWidget((error, result, cloudWidget) => {
                if(!error && result && result.event === "success"){
                    if(result.info.resource_type !== 'image'){
                        cloudWidget.close()
                        return alert('Please upload an image')
                    }                    
                    const response = super.f(`set-avatar?url=${result.info.url}`, 'POST')
                    if(response.success)
                        this.img.src = result.info.url
                }
            })
        }

        this.changePasswordModal.onclick = e => {
            if(e.target === e.currentTarget)
                this.changePasswordModal.classList.add('hide')  
        }

        this.changePasswordModal.querySelector('.close-modal').onclick = e => this.changePasswordModal.classList.add('hide')

        this.submitChangeButton.onclick = async e => {
            e.preventDefault()
            for(const li of this.changePasswordModal.querySelectorAll('li'))
                li.remove()

            const data = {
                oldPassword: this.oldPasswordInput.value,
                newPassword: this.newPasswordInput.value
            }

            const response =  await super.f('change-password', 'PUT', JSON.stringify(data))
            if(response.success){
                this.setCookie('my-chat-app-jwt', response.data, 30)
                this.stomp.send(`/app/global/${this.id}`, {}, JSON.stringify({type: 'RELOAD', data: null}))
            }

            else{
                if(response.error instanceof Array){
                    response.error.forEach(errorMsg => {
                        const li = document.createElement('li')
                        li.innerText = errorMsg.substring(13)

                        if(errorMsg.startsWith('oldPassword'))
                            this.invalidOldPasswordMsg.appendChild(li)
                        else
                            this.invalidNewPasswordMsg.appendChild(li)
                    })                    
                }

                else{
                    const li = document.createElement('li')
                    li.innerText = response.error
                    this.invalidOldPasswordMsg.appendChild(li)
                }
            }

        }
    }

    loadInfo(){
        super.f('users/me')
            .then(response => response.data)
            .then(data => {
                this.component.querySelector('#username').innerText = data.username
                this.component.querySelector('#user-id').innerText = `ID: ${this.id}`
                this.img.src = data.avatarUrl || 'default.jpg'
            })
    }


    setCookie(name, value, days){
        const expires = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString();
        document.cookie = `${name}=${value}; expires=${expires}; path=/; secure; SameSite=Strict`;
    };


}