/**
 * Login management
 */

(function() {
	document.getElementById("submit").onclick = (e) => {
		var form = e.target.closest("form")
		if (form.checkValidity()) {
			makeCall("POST", "CheckLogin", form, (x) => {
				if (x.readyState == XMLHttpRequest.DONE) {
					// TODO: manca il caso credenziali sbagliate
					let msg = x.responseText;
					switch (x.status) {
						case 200:
							sessionStorage.setItem("currentUser", msg)
							window.location.href = "onePage.html"
							break
						case 400:
						case 401:
						case 500:
							alert(msg)
							form.reset()
							break
					}
				} else form.reportValidity()
			})
		}
	}
})();