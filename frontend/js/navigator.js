import eventBus from "./eventBus.js"

export default class Navigator {
    constructor(app){
        this.app = app

        this.component = app.querySelector('#side-bar')
        this.currentFocus = this.component.querySelector('.active')        

        this.currentTab = document.getElementById(`${this.currentFocus.getAttribute('navigate')}`)

        this.addListener()
    }

    addListener(){
        eventBus.listen('navigateTo', data => {
            const id = data.id
            this.component.querySelector(`[navigate="${id}"]`).click()
        })


        this.component.querySelector('[navigate="chat-tab"]').onclick = e => {
            if(this.currentFocus === e.currentTarget)
                return 

            const chatTab = this.app.querySelector('#chat-tab')

            this.currentFocus.classList.remove('active')
            e.currentTarget.classList.add('active')
            
            this.currentTab.classList.add('hide')
            chatTab.querySelector('#conversation-bar').classList.remove('hide')
            this.currentTab = chatTab
            this.currentFocus = e.currentTarget
        }

        this.component.querySelector('[navigate="friend-tab"]').onclick = e => this.handleClick(e)

        this.component.querySelector('[navigate="create-group-tab"]').onclick = e => this.handleClick(e)
        
    }

    handleClick(e){
        if(this.currentFocus === e.currentTarget)
            return 

        const tab = this.app.querySelector(`#${e.currentTarget.getAttribute("navigate")}`)

        this.currentFocus.classList.remove('active')
        e.currentTarget.classList.add('active')
        
        if(this.currentTab.id === 'chat-tab')
            this.currentTab.querySelector('#conversation-bar').classList.add('hide')
        else
            this.currentTab.classList.add('hide')

        tab.classList.remove('hide')

        this.currentTab = tab
        this.currentFocus = e.currentTarget
    }
}