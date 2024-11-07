export default async function render(app) {
    let ok = true
    await fetch('./components/sideBar.html')
        .then(response => response.text())
        .then(html => app.innerHTML += html)
        .catch(() => ok = false)

    await fetch('./components/chatTab.html')
        .then(response => response.text())
        .then(html => app.innerHTML += html)
        .catch(() => ok = false)

    await fetch('./components/friendTab.html')
        .then(response => response.text())
        .then(html => app.innerHTML += html)
        .catch(() => ok = false)

    await fetch('./components/createGroupTab.html')
        .then(response => response.text())
        .then(html => app.innerHTML += html)
        .catch(() => ok = false)

    await fetch('./components/changePasswordModal.html')
        .then(response => response.text())
        .then(html => app.innerHTML += html)
        .catch(() => ok = false)

    await fetch('./components/accountDetail.html')
        .then(response => response.text())
        .then(html => app.innerHTML += html)
        .catch(() => ok = false)
        
    return ok
}