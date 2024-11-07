import eventBus from "./eventBus.js"
import Tab from "./Tab.js"

export default class FriendTab extends Tab {
    constructor(app, token, id, stomp) {
        super()
        this.app = app
        this.token = token
        this.id = id
        this.stomp = stomp

        this.component = app.querySelector('#friend-tab')
        this.friendList = app.querySelector('#friend-bar')

        this.friendUl = this.component.querySelector('#friend-list')

        this.searchInput = this.component.querySelector('#friend-search input')
        this.searchSubmit = this.component.querySelector('#friend-search i')
        this.currentPage = 0
        this.lastPage = true
        this.currentSearchValue = ''
        
        this.friendStatusMap = {}

        this.subscribeToTopic()

        this.renderAllFriend()

        this.addListener()
    }

    addListener(){
        this.searchInput.onkeydown = e => {
            if(e.key === 'Enter'){
                this.handleSubmitSearch()
            }
        }

        this.searchSubmit.onclick = e => {
            this.handleSubmitSearch()
        }

        this.component.querySelector('.tab-side-bar').addEventListener('wheel', e => {
            if (e.currentTarget.scrollTop + e.currentTarget.clientHeight < 2/3 * e.currentTarget.scrollHeight) {
                return;
            }

            this.searchAndRender(this.currentSearchValue)                    
        })

        eventBus.listen('createGroup', data => {
            console.log(data);
            
            this.sendNotification(data.toId, 'START_CHAT', data.conversationId)
        })
    }

    handleSubmitSearch(){
        this.lastPage = false
        this.currentPage = 0
        this.removeAllFriend()
        this.currentSearchValue = this.searchInput.value
        this.searchAndRender(this.currentSearchValue)
    }

    async searchAndRender(name){
        if(this.lastPage)
            return

        const data = await super.f(`users/search?username=${name}&page=${this.currentPage}&size=20`)
                    .then(response => response.data)

        if(!data.last)
            this.currentPage++
        this.lastPage = data.last
        
        for(const user of data.content){
            if(this.friendStatusMap[user.id]){
                if(this.friendStatusMap[user.id].code === 1)
                    user.status = 'FRIEND'
                else if(this.friendStatusMap[user.id].code === 2)
                    user.isFromMe = true
                user.updatedAt = this.friendStatusMap[user.id].updatedAt
                this.appendFriend(user)
            }

            else{
                const strangerElement = document.createElement('li')
                strangerElement.classList.add('tab-side-bar-li')
                strangerElement.classList.add('stranger')
                strangerElement.setAttribute('user-id', user.id)
                strangerElement.innerHTML = `
                                    <img src=${user.avatarUrl || "./default.jpg"} alt="avt" class="chat-item__avatar">
                                    <div class="tab-side-bar-li__body">
                                        <h3 class="friend-name">${user.username}</h3>
                                        <div class="friend-item-features">
                                            <button class="friend-item-feature add-friend">
                                                <i class="fa-solid fa-user-plus"></i>
                                            </button>
                                        </div>
                                    </div>
                                    <div class="tab-side-bar-li__info">
                                        <span class="info-time"></span>
                                    </div>`

                strangerElement.querySelector('.add-friend').onclick = e => this.sendAddRequest(user.id, strangerElement)
                

                this.friendUl.appendChild(strangerElement)
            }
        }

    }

    removeAllFriend(){
        for(const li of this.friendUl.querySelectorAll('li')){
            li.remove()
        }
    }

    async renderAllFriend(){
        const data = await super.f(`friend/all`)
                    .then(response => response.data)      

        const friendList = []
        const receivedList = []
        const pendingList = []
        data.forEach(friendData => {
            if(friendData.status === 'FRIEND'){ 
                friendList.push(friendData)
                this.friendStatusMap[friendData.userId] = {code: 1, updatedAt: friendData.updatedAt}
            }
            else if(friendData.isFromMe === true){ 
                pendingList.push(friendData)
                this.friendStatusMap[friendData.userId] = {code: 2, updatedAt: friendData.updatedAt}
            }
            else {
                receivedList.push(friendData)
                this.friendStatusMap[friendData.userId] = {code: 3, updatedAt: friendData.updatedAt}
            }
        });

        friendList.forEach(friendData => this.appendFriend(friendData))
        receivedList.forEach(friendData => this.appendFriend(friendData))
        pendingList.forEach(friendData => this.appendFriend(friendData))
    }

    appendFriend(friendData){
        const li = document.createElement('li')
        
        li.classList.add('tab-side-bar-li')
        li.classList.add(friendData.status === 'FRIEND' ? 'is-friend' : friendData.isFromMe ? 'pending' : 'received')
        li.setAttribute('user-id', friendData.userId)

        li.innerHTML = `
                <img src=${friendData.avatarUrl || "./default.jpg"} alt="avt" class="chat-item__avatar">
                <div class="tab-side-bar-li__body">
                    <h3 class="friend-name">${friendData.username}</h3>
                    <p class="pending-notification">Add friend request has been sent</p>
                    <div class="friend-item-features">
                        <button class="friend-item-feature start-chat"><i class="fa-solid fa-message"></i></button>
                        <button class="friend-item-feature unfriend"><i class="fa-solid fa-trash"></i></button>
                        <button class="friend-item-feature accept"><i class="fa-solid fa-check"></i></i></button>
                        <button class="friend-item-feature refuse"><i class="fa-solid fa-x"></i></button>
                    </div>
                </div>
                <div class="tab-side-bar-li__info">
                    <span class="info-time">${super.toNormailizeDate(friendData.updatedAt)}</span>
                </div>`

        this.friendUl.appendChild(li)
        this.addFeatureEvent(li)
    }

    // 
    addFeatureEvent(friendElement){
        const partnerId = friendElement.getAttribute('user-id')

        if(friendElement.classList.contains('is-friend')){
            friendElement.querySelector('.start-chat').onclick = async e => {
                let response = await super.f(`conversation/get?userId=${partnerId}`)

                if(!response.success){
                    response = await super.f(`conversation/create?partnerId=${partnerId}`, 'POST')
                    if(response.success){
                        this.sendNotification(this.id, 'START_CHAT', response.data.id)
                        this.sendNotification(partnerId, 'START_CHAT', response.data.id)
                    }
                    return
                }
                
                eventBus.dispatch('navigateTo', {id: 'chat-tab'})
                eventBus.dispatch('toConversation', {conversationId: response.data.id})
            }

            friendElement.querySelector('.unfriend').onclick = async e => {
                const response = await super.f(`friend/unfriend?friendId=${partnerId}`, 'DELETE')

                if(response.success){
                    friendElement.remove()
                    this.sendNotification(partnerId, 'UNFRIEND')
                    this.friendStatusMap[partnerId] = null
                }
            }
        }

        else if(friendElement.classList.contains('received')){
            friendElement.querySelector('.accept').onclick = async e => {
                const response = await super.f(`friend/accept?senderId=${partnerId}`, 'PUT')
                if(response.success){
                    friendElement.classList.remove('received')
                    friendElement.classList.add('is-friend')
                                        
                    friendElement.querySelector('.info-time').innerText = super.toNormailizeDate(response.data)

                    this.sendNotification(partnerId, 'ACCEPT_ADD_FRIEND', response.data)
                    this.friendStatusMap[partnerId] = {code: 1, updatedAt: response.data.updatedAt}

                    return this.addFeatureEvent(friendElement)
                }
            }

            friendElement.querySelector('.refuse').onclick = async e => {
                const response = await super.f(`friend/refuse?senderId=${partnerId}`, 'DELETE')
                if(response.success){
                    friendElement.remove()
                    this.sendNotification(partnerId, 'REFUSE_ADD_FRIEND')
                    this.friendStatusMap[partnerId] = null
                }
            }
        }

    }

    // 
    sendNotification(toId, type, bonus = null){
        console.log(toId);
        
        console.log(bonus);
        const data = {
            fromId: this.id, 
            type,
            bonus
        }
        this.stomp.send(`/app/to-user/${toId}`, {}, JSON.stringify(data))
    }

    // 
    rerenderFriend(friendElement, friendData){
        friendElement.classList.remove('stranger')
        friendElement.classList.add('pending')
        friendElement.innerHTML = `
                    <img src=${friendData.avatarUrl || "./default.jpg"} alt="avt" class="chat-item__avatar">
                    <div class="tab-side-bar-li__body">
                        <h3 class="friend-name">${friendData.username}</h3>
                        <p class="pending-notification">Add friend request has been sent</p>
                        <div class="friend-item-features">
                            <button class="friend-item-feature start-chat"><i class="fa-solid fa-message"></i></button>
                            <button class="friend-item-feature unfriend"><i class="fa-solid fa-trash"></i></button>
                            <button class="friend-item-feature accept"><i class="fa-solid fa-check"></i></i></button>
                            <button class="friend-item-feature refuse"><i class="fa-solid fa-x"></i></button>
                        </div>
                    </div>
                    <div class="tab-side-bar-li__info">
                        <span class="info-time">${super.toNormailizeDate(friendData.updatedAt)}</span>
                    </div>`

        this.addFeatureEvent(friendElement)
        this.friendStatusMap[friendData.userId] = {code: 2, updatedAt: friendData.updatedAt}
    }

    // 
    async sendAddRequest(userId, friendElement){
        const response = await super.f(`friend/add?receiverId=${userId}`, 'POST')        
        if(response.success){
            this.sendNotification(userId, 'ADD_FRIEND', response.data)
            
            response.data.userId = userId
            response.data.avatarUrl = friendElement.querySelector('.chat-item__avatar').src
            this.rerenderFriend(friendElement, response.data)            
        }
    }

    // 
    subscribeToTopic() {
        this.stomp.subscribe(`/notification/${this.id}`, (message) => {
            const msg = JSON.parse(message.body)
            
            if(msg.type === 'START_CHAT'){
                eventBus.dispatch('addConversation', {fromId: msg.fromId, bonus: msg.bonus})
            }

            else if(msg.type === 'ACCEPT_ADD_FRIEND'){
                const friendElement = this.friendUl.querySelector(`[user-id="${msg.fromId}"]`)
                friendElement.classList.remove('pending')
                friendElement.classList.add('is-friend')                    
                friendElement.querySelector('.info-time').innerText = super.toNormailizeDate(msg.bonus)
                this.addFeatureEvent(friendElement)
                this.friendStatusMap[msg.fromId] = {code: 1, updatedAt: msg.bonus}
            }

            else if(msg.type === 'UNFRIEND' || msg.type === 'REFUSE_ADD_FRIEND'){
                const friendElement = this.friendUl.querySelector(`[user-id="${msg.fromId}"]`)
                if(friendElement){
                    friendElement.remove()
                    this.friendStatusMap[msg.fromId] = null
                }
            }

            else if(msg.type === 'ADD_FRIEND'){
                msg.bonus.isFromMe = false
                msg.bonus.userId = msg.fromId
                msg.bonus.username = msg.fromName
                
                const friendElement = this.friendUl.querySelector(`[user-id="${msg.fromId}"]`)
                console.log(msg.bonus);
                
                if(!friendElement)
                    this.appendFriend(msg.bonus)
                else
                    this.rerenderFriend(friendElement, msg.bonus)
                this.friendStatusMap[msg.fromId] = {code: 3, updatedAt: msg.bonus.updatedAt}
            }

        })
    }
}