/**
 * Login management
 */

(function () {
	document.getElementById("submit").onclick = (e) => {
		e.preventDefault()
		var form = e.target.closest("form")
		if (form.checkValidity()) {
			makeCall("POST", "CheckLogin", form, (x) => {
				if (x.readyState == XMLHttpRequest.DONE) {
					let msg = x.responseText;
					switch (x.status) {
						case 200:
							sessionStorage.setItem("currentUser", msg)
							window.location.href = "onePage.html"
							break
						case 400: // bad request
							alert("There cannot be empty fields")
							window.location.href = "login.html"
							break
						case 401: // unauthorized
							alert("Wrong username or password")
							form.reset()
							break
						case 500: // server error
							alert("An error occurred, try again later")
							form.reset()
							break
						default:
							console.log(msg)
							form.reset()
					}
				} else form.reportValidity()
			})
		}
	}
})();