export default class Tab {
    constructor(){}

    async f(path, method = "GET", body = null){
        return fetch(`http://localhost:8080/${path}`, {
            method,
            headers: {
                "Authorization": `Bearer ${this.token}`,
                "Content-Type": "application/json"
            },
            body
        })
        .then(response => response.json())
    }

    // 
    toNormailizeDate(date){

        const dateObj = new Date(date);

        const formattedDate = dateObj.toLocaleDateString('vi-VN') + ' \n ' + dateObj.toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });

        return formattedDate
    }

    // 
    toNormailizeDateTimestamp(timestamp) {
        const date = new Date(timestamp);
    
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
    
        return `${day}/${month}/${year} \n ${hours}:${minutes}`;
    }
}