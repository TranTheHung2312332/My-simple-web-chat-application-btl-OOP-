import Tab from "./Tab.js"
import eventBus from "./eventBus.js"

export default class CreateGroupTab extends Tab {
    constructor(app, token, id, stomp){
        super()
        this.app = app
        this.token = token
        this.id = id
        this.stomp = stomp

        this.component = app.querySelector('#create-group-tab')
        this.friendUl = this.component.querySelector('#friend-list-group-tab')

        this.submitButton = this.component.querySelector('#group-name-input i')
        this.nameInput = this.component.querySelector('#group-name-input input')
        this.setAvatarButton = this.component.querySelector('#avatar-attach')
        
        this.currentAvatar = 'group_chat_default.jpg'
        
        this.renderAllFriend()
        this.addListener()
    }

    addListener(){
        this.nameInput.onkeydown = e => {
            if(e.key === 'Enter'){
                this.handleSubmitCreate()
            }
        }

        this.submitButton.onclick = e => {
            this.handleSubmitCreate()
        }

        this.setAvatarButton.onclick = e => {
            initializeUploadWidget((error, result, cloudWidget) => {
                if(!error && result && result.event === "success"){
                    if(result.info.resource_type !== 'image'){
                        cloudWidget.close()
                        return alert('Please upload an image')
                    }                    
                    this.currentAvatar = result.info.url
                    cloudWidget.close()
                    this.setAvatarButton.classList.add('active')
                }
            })
        }
    }

    async renderAllFriend(){
        const data = await super.f(`friend/accepted`)
                .then(response => response.data)      

        data.forEach(friendData => this.appendFriend(friendData))
    }


    appendFriend(friendData){
        const li = document.createElement('li')
        
        li.classList.add('tab-side-bar-li')
        li.setAttribute('user-id', friendData.userId)

        li.innerHTML = `
                <img src=${friendData.avatarUrl || "./default.jpg"} alt="avt" class="chat-item__avatar">
                <div class="tab-side-bar-li__body">
                    <h3 class="friend-name">${friendData.username}</h3>
                    <p class="friend-id">ID: ${friendData.userId}</p>
                </div>
                <input type="checkbox" name="" id="add-user-with-id${friendData.userId}" class="add-member-checkbox">`

        this.friendUl.appendChild(li)
    }

    async handleSubmitCreate(){
        const checkedList = this.friendUl.querySelectorAll('li input[type="checkbox"]:checked')
        if(!checkedList.length)
            return
        
        const memberIds = Array.from(checkedList).map(input => Number(input.id.substring(16)))
        const data = {
            name: this.nameInput.value || null,
            members: memberIds,
            avatarUrl: this.currentAvatar
        }        
        
        const response = await super.f('conversation/create-group', 'POST', JSON.stringify(data))
        
        if(response.success){
            this.currentAvatar = 'group_chat_default.jpg'
            this.setAvatarButton.classList.remove('active')
            checkedList.forEach(input => input.checked = false)
            this.nameInput.value = ''
            
            eventBus.dispatch('createGroup', {toId: this.id, conversationId: response.data.id})
            memberIds.forEach(id => eventBus.dispatch('createGroup', {toId: id + '', conversationId: response.data.id}))
        }
    }

}