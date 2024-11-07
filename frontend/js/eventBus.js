const eventBus = {
    dispatch(eventName, data) {
        const event = new CustomEvent(eventName, { detail: data });
        document.dispatchEvent(event);
    },
    listen(eventName, callback) {
        document.addEventListener(eventName, (event) => {
            callback(event.detail);
        });
    }
}

export default eventBus