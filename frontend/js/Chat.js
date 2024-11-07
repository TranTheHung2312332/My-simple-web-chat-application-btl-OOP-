import Tab from "./Tab.js"
import eventBus from "./eventBus.js"

export default class Chat extends Tab {
    constructor(app, token, id, stomp){
        super()
        this.stomp = stomp
        this.token = token
        this.app = app
        this.id = id
        this.currentConversationId = 0

        this.component = app.querySelector('#chat-tab')        
        
        this.searchInput = this.component.querySelector('#chat-search input')

        this.searchSubmit = this.component.querySelector('#chat-search i')
        
        this.chatList = this.component.querySelector('#chat-list')

        this.chatScreen = this.component.querySelector('.chat-screen')
        this.chatHeaderContent = this.component.querySelector('#chat-main header')

        this.chatInput = this.component.querySelector('#chat-input')
        this.sendButton = this.component.querySelector('#send-message-button')

        this.subscriptions = {}

        this.insertImgButton = this.component.querySelector('#chat-attach')

        
        this.previewContainer = this.component.querySelector('#chat-image-preview')
        this.previewImg = this.component.querySelector('#chat-image-preview img')
        this.deletePreviewImg = this.component.querySelector('#chat-image-preview i')

        this.previewImg.src = null

        this.renderAllConversations()
        
        this.addListener()
    }

    // 
    addListener(){
        this.searchInput.onkeydown = e => {
            if(e.key === 'Enter')
                this.search(e.target.value)
        }

        this.searchSubmit.onclick = e => this.search(this.searchInput.value)

        this.chatInput.onkeydown = e => {
            if(e.key === 'Enter'){
                this.sendMessage(this.currentConversationId, e.target.value)
                e.target.value = ''
            }
        }

        this.sendButton.onclick = e => {
            this.sendMessage(this.currentConversationId, this.chatInput.value)
            this.chatInput.value = ''
        }

        eventBus.listen('toConversation', data => {
            this.chatList.querySelector(`[conversation-id="${data.conversationId}"]`).click()
        })

        eventBus.listen('addConversation', async data => {
            if(!Object.keys(this.subscriptions).length){
                await this.renderAllConversations()
                // this.chatScreen.querySelector('.message').remove()
            }
            else
                await this.appendConversation(data.bonus)

            console.log(data.bonus);
            
            if(data.fromId == this.id){
                eventBus.dispatch('navigateTo', {id: 'chat-tab'})
                this.chatList.querySelector(`[conversation-id="${data.bonus.id}"]`).click()
                this.chatInput.value = 'Hello'
                this.sendButton.click()
            }
        })

        this.deletePreviewImg.onclick = e => this.clearPreviewImg()

        this.insertImgButton.onclick = e => {
            initializeUploadWidget((error, result, cloudWidget) => {
                if(!error && result && result.event === "success"){
                    if(result.info.resource_type !== 'image'){
                        cloudWidget.close()
                        return alert('Please upload an image')
                    }
                    cloudWidget.close()
                    this.previewContainer.classList.remove('hide')
                    this.previewImg.src = result.info.url
                }
            })
        }
    }

    // 
    clearPreviewImg(){
        this.previewContainer.classList.add('hide')
        this.previewImg.src = null
    }

    // 
    removeAllMessages(){
        for(const messageElement of this.chatScreen.querySelectorAll('.message')){
            messageElement.remove()
        }
    }

    // 
    removeAllConversation(){
        for(const li of this.chatList.querySelectorAll('li')){
            li.remove()
        }
    }

    // 
    search(value){
        if(value)
            this.renderAllConversations(value)
    }

    // 
    async sendMessage(conversationId, content){ 
        let imageUrl = null
        const src = this.previewImg.src;

        if (!src.includes("/null"))
            imageUrl = src

        if(!content && !imageUrl)
            return

        const message = {
            conversationId: Number(conversationId),
            content,
            imageUrl
        }            

        this.clearPreviewImg()        
        const response = await super.f(`message/post`, 'POST', JSON.stringify(message))        
        
        response.data.messageId = response.data.id
        if(response.success){
            this.stomp.send(`/app/message/${response.data.conversationId}`, {}, JSON.stringify(response.data))
        }

    }

    // 
    async renderAllConversations(conversationName){
        this.removeAllConversation()
        let path = "conversation/all"
        if(conversationName)
            path = `conversation/search?name=${conversationName}`

        const data = await super.f(path)
                    .then(response => response.data)

        let hasFirstConversation;

        data.forEach(conversation => {
            this.appendConversation(conversation)

            hasFirstConversation = true
        })

        this.chatList.querySelectorAll('.tab-side-bar-li').forEach(li => {
            const conversationId = li.getAttribute('conversation-id')
            super.f(`message/last?conversationId=${conversationId}`)
                .then(response => response.data)
                .then(lastMessage => {
                    li.setAttribute('last-message-id', lastMessage.id)

                    const sender = lastMessage.senderId != this.id ? lastMessage.senderName : 'You'
                    li.querySelector('.chat-item__desc').innerText = `${sender}: ${lastMessage.content}`
                    li.querySelector('.info-time').innerText = this.toNormailizeDate(lastMessage.createdAt)  

                    if(lastMessage.status === 'DELIVERED')
                        li.classList.add('not-seen')
                })
                .catch(e => li.querySelector('.chat-item__desc').innerText = `Start your conversation`)
        })

        const firstConversation = this.component.querySelector('.tab-side-bar-li')
        if(hasFirstConversation){
            this.currentConversationId = firstConversation.getAttribute("conversation-id")
            firstConversation.classList.add('focus')
            const name = firstConversation.querySelector('.chat-item__name').innerText
            this.renderScreen(firstConversation.getAttribute("conversation-id"), name)
        }

    }

    // 
    async renderScreen(conversationId, conversationName){
        try{
            this.removeAllMessages()
            this.chatHeaderContent.querySelector('.header__name').innerText = conversationName

            const data = await super.f(`message/all?conversationId=${conversationId}`)
                        .then(response => response.data)
                
            data.forEach(message => this.appendMessage(message))
            this.chatScreen.scrollTop = this.chatScreen.scrollHeight
        } catch(e){}
    }

    // 
    async appendConversation(conversation){
        let name
        let defaultAvatar = 'group_chat_default.jpg'
        let avatarUrl
        if(conversation.conversationType === 'GROUP'){
            name = conversation.name
            avatarUrl = conversation.avatarUrl
        }
        else{
            defaultAvatar = 'default.jpg'
            for(const user of conversation.participants){
                if(user.id != this.id){
                    name = user.username
                    avatarUrl = user.avatarUrl
                    break
                }
            }
        }

        const li = document.createElement('li')
        li.classList.add('tab-side-bar-li')
        li.setAttribute("conversation-id", conversation.id)
        li.innerHTML = `<img src=${avatarUrl || defaultAvatar} alt="avt" class="chat-item__avatar">
                        <div class="tab-side-bar-li__body">
                            <h3 class="chat-item__name">${name}</h3>
                            <p class="chat-item__desc"></p>
                        </div>
                        <div class="tab-side-bar-li__info">
                            <span class="info-time"></span>
                        </div>`

        li.onclick = e => this.changeConversation(e.currentTarget)

        this.chatList.appendChild(li)

        this.subscribeToTopic(conversation.id)
    }

    // 
    appendMessage(message){
        const messageElement = document.createElement('div')
        
        messageElement.classList.add('message')
        if(message.senderId == this.id)
            messageElement.classList.add('from-me')
            messageElement.innerHTML = `
                                    <div class="message-avatar"><img src="${message.senderAvatarUrl || 'default.jpg'}" alt=""></div>
                                    <div class="message-body">
                                        <h2 class="message-sender">${message.senderName}</h2>
                                        ${message.imageUrl ? `<img src="${message.imageUrl}" alt="">` : ''}
                                        <p class="message-content">${message.content}</p>
                                    </div>`

        this.chatScreen.appendChild(messageElement)  
    }

    // 
    updateLastMessage(message){
        const conversation = this.chatList.querySelector(`[conversation-id="${message.conversationId}"]`)

        conversation.setAttribute('last-message-id', message.id)
        
        let senderName = message.senderName
        const sennderId = message.senderId
        const content = message.content

        if(sennderId == this.id)
            senderName = 'You'
        else
            conversation.classList.add('not-seen')

        conversation.querySelector('.chat-item__desc').innerText = `${senderName}: ${content}`
        conversation.querySelector('.info-time').innerText = super.toNormailizeDate(message.createdAt)

        if(message.conversationId == this.currentConversationId)
            this.updateMessageStatus(conversation)
    }

    // 
    changeConversation(conversation){
        const conversationId = conversation.getAttribute("conversation-id")
        
        if(conversationId == this.currentConversationId)
            return

        this.updateMessageStatus(conversation)
                
        this.chatList.querySelector(`[conversation-id="${this.currentConversationId}"]`).classList.remove('focus')
        this.currentConversationId = conversationId

        this.renderScreen(conversationId, conversation.querySelector('.chat-item__name').innerText)

        conversation.classList.add('focus')
    }

    
    async updateMessageStatus(conversation){
        if(conversation.classList.contains('not-seen')){
            const response = await super.f(`message/update-status?messageId=${conversation.getAttribute('last-message-id')}`, 'PUT')
            if(!response.success)
                return
            conversation.classList.remove('not-seen')
        }
    }

    subscribeToTopic(conversationId) {
        const subscription = this.stomp.subscribe(`/chanel/${conversationId}`, (message) => {
            const msg = JSON.parse(message.body)
            if(this.currentConversationId == conversationId)
                this.appendMessage(msg)
            this.updateLastMessage(msg)

            if(msg.senderId == this.id)
                this.chatScreen.scrollTop = this.chatScreen.scrollHeight
        })
        
        this.subscriptions[conversationId] = subscription
    }
    
    unsubscribeFromTopic(conversationId) {
        if (this.subscriptions[conversationId]) {
            this.subscriptions[conversationId].unsubscribe()
            delete this.subscriptions[conversationId]
            console.log(`Unsubscribed from topic ${conversationId}`)
        }
    }
}