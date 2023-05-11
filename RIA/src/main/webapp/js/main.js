var id_from = null
var id_to = null

{	// avoid other variables ending up in the global scope

	// page components
	let treeDiv, formContainer, imgContainer,
		pageOrchestrator = new PageOrchestrator() // main controller

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("currentUser") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start(); // initialize the components
			pageOrchestrator.refresh();
		} // display initial content
	}, false);

	// Constructor of view components

	function Tree(_container, _alertContainer) {
		this.container = _container
		this.alertContainer = _alertContainer

		document.getElementById("confirm").onclick = () => {
			if (id_from != null && id_to != null) {
				this.alertContainer.style.visibility = "hidden"

				makeCall("GET", "CopyHere?id_from=" + id_from + "&id_to=" + id_to, null, (req) => {
					if (req.readyState == 4) {
						let msg = req.responseText
						if (req.status == 200) {
							this.reset()
							id_from = null
							id_to = null
						} else {
							this.alert.textContent = msg
						}
					}
				})
			}
		}

		document.getElementById("revert").onclick = () => {
			id_from = null
			id_to = null
			this.alertContainer.style.visibility = "hidden"
			this.reset()
		}

		this.reset = () => {
			this.show()
		}

		this.show = () => {
			var self = this
			self.alertContainer.style.visibility = "hidden"
			makeCall("GET", "GetTree", null, (req) => {
				if (req.readyState == 4) {
					let msg = req.responseText;
					if (req.status == 200) {
						let treeObject = JSON.parse(msg);
						self.update(treeObject)
						formContainer.update(treeObject)
					} else {
						self.alert.textContent = msg;
					}
				}
			})
		}

		this.update = (obj) => {
			this.container.innerHTML = ""
			var rootList = document.createElement("ul")
			obj.forEach((o) => this.rec(o, rootList))
			this.container.appendChild(rootList)
		}

		this.rec = (node, list) => {
			let self = this;

			// Recursive call
			let elm = document.createElement("ul")
			if (node.children.length != 0)
				node.children.forEach(o => this.rec(o, elm))

			let li = document.createElement("li")
			let name = document.createTextNode(node.name)
			let hierarchy = document.createTextNode(node.hierarchy + ". ")
			let spH = document.createElement("span")
			spH.classList.add("h")

			let dragText = document.createTextNode(" dragMe")
			let spD = document.createElement("span")
			spD.classList.add("d")
			spD.setAttribute("draggable", "true")
			li.setAttribute("id", "category" + node.id)
			spD.addEventListener("dragstart", (e) => {

				let mainList = self.container.querySelector("ul")
				let specialText = document.getElementById("copyInRoot")
				if (mainList.childElementCount < 9) {
					specialText.style.visibility = "visible"

					specialText.addEventListener("dragover", (e) => {
						e.preventDefault()
					})

					specialText.addEventListener("drop", (e) => {
						e.preventDefault()
						e.stopPropagation()
						let data = e.dataTransfer.getData("text/plain")
						let dragged = document.getElementById("category" + data)

						let from = dragged != null ? dragged.querySelector(".n").innerHTML : null
						let to = "the main root"


						let c = confirm("sure you want to append " + from + " to " + to + "?")
						if (dragged != null && c) {
							// just copy the dragged element and append it to the list
							// but make it red
							let copy = dragged.cloneNode(true)
							copy.style.color = "red"
							copy.querySelectorAll(".h").forEach((f) => f.innerHTML = "-")
							document.querySelectorAll(".d").forEach((f) => f.style.display = "none")
							specialText.style.visibility = "hidden"

							mainList.appendChild(copy)
							self.alertContainer.style.visibility = "visible"
							id_from = parseInt(data)
							id_to = -1

						}
						else this.reset()
					})
				}

				e.dataTransfer.setData("text/plain", node.id)
				document.querySelectorAll(".d").forEach((f) => {
					if (spD != f && (document.getElementById("category" + node.id).contains(f) || f.parentNode.lastChild.childElementCount >= 9)) {
						f.style.display = "none"
					} else if (spD == f) {
						f.innerHTML = " dropMe"
					} else {
						f.innerHTML = " dropHere"
					}
				})
			})
			spD.addEventListener("dragover", (e) => {
				e.preventDefault()
			})
			spD.addEventListener("drop", (e) => {
				e.preventDefault()
				e.stopPropagation()
				let data = e.dataTransfer.getData("text/plain")
				let dragged = document.getElementById("category" + data)

				let from = dragged != null ? dragged.querySelector(".n").innerHTML : null
				let to = node.name

				if (node.id == parseInt(data)) {
					alert("Mi dispiace, impossibile copiare un sottoalbero in sé stesso :(")
					this.reset()
					return
				}

				let c = confirm("sure you want to append " + from + " to " + to + "?")
				if (dragged != null && c) {
					// just copy the dragged element and append it to the list
					// but make it red
					let copy = dragged.cloneNode(true)
					copy.style.color = "red"
					copy.querySelectorAll(".h").forEach((f) => f.innerHTML = "-")
					document.querySelectorAll(".d").forEach((f) => f.style.display = "none")
					document.getElementById("copyInRoot").style.visibility = "hidden"

					li.querySelector("ul").appendChild(copy)
					self.alertContainer.style.visibility = "visible"
					id_from = parseInt(data)
					id_to = node.id

				}
				else this.reset()
			})

			let sp = document.createElement("span")
			sp.classList.add("n")
			sp.addEventListener("click", (e) => {
				e.preventDefault()
				// replace the text with a text input
				console.log("clicked something")
				let input = document.createElement("input")
				input.type = "text"
				input.value = e.target.innerHTML
				input.addEventListener("blur", (f) => {
					// send the new name to the server
					let newName = f.target.value
					makeCall("GET", "RenameCategory?id=" + node.id + "&newName=" + newName, null, (req) => {
						if (req.readyState == 4) {
							let msg = req.responseText
							if (req.status == 200) {
								console.log("New name = " + newName)
								name = document.createTextNode(newName)
								sp.innerHTML = newName
								li.replaceChild(sp, input)
							} else {
								// TODO: gestire errore
							}
						}
					})
				})
				li.replaceChild(input, sp)
				input.focus()
			})

			let aOpenCategory = document.createElement("a")
			let spO = document.createElement("span")
			aOpenCategory.href = "#"
			aOpenCategory.innerHTML = " Apri"
			aOpenCategory.addEventListener("click", (e) => {
				e.preventDefault()
				imgContainer.show(node.id)
			})

			spH.appendChild(hierarchy)
			spD.appendChild(dragText)
			spO.appendChild(aOpenCategory)
			sp.appendChild(name)
			li.appendChild(spH)
			li.appendChild(sp)
			li.appendChild(spO)
			li.appendChild(spD)
			li.appendChild(elm)
			list.appendChild(li)
		}
	}

	function ImgContainer(_container) {
		this.container = _container

		this.reset = () => {
			this.container.querySelector(".imgContainer").innerHTML = ""
		}

		this.show = (category_id) => {
			let self = this
			document.querySelector("input[type=hidden]").value = category_id
			makeCall("GET", "OpenCategory?id=" + category_id, null,
				(req) => {
					if (req.readyState == 4) {
						var msg = req.responseText
						if (req.status == 200) {
							var imageList = JSON.parse(msg)
							self.update(imageList)
						} else {
							// TODO: gestire caso di login non fatto
						}
					}
				})

			this.container.style.visibility = "visible"
			this.container.querySelector(".imgContainer").style.visibility = ""


			this.container.querySelector("form").addEventListener("submit", (e) => {
				e.preventDefault()
				let form = e.target.closest("form")
				if (form.checkValidity()) {
					let formData = new FormData(form)
					// transform the input name image into a blob
					//let file = form.querySelector("input[type=file]").files[0]
					//formData.append("image", file)
					makeCall("POST", "UploadImage", formData,
						(req) => {
							if (req.readyState == 4) {
								var msg = req.responseText
								if (req.status == 200) {
									console.log("Image uploaded")
									self.show(category_id)
								} else {
								}
							}
						})
				}
			})
		}

		this.update = (imageList) => {
			// show the list of images (coded as blobs) in the imgContainer
			let imgContainer = this.container.querySelector(".imgContainer")
			imgContainer.innerHTML = ""
			imageList.forEach((img) => {
				//console.log(img)
				let imgDiv = document.createElement("div")
				let imgElm = document.createElement("img")
				imgElm.src = "data:image/png;base64," + img.img
				imgDiv.appendChild(imgElm)
				imgContainer.appendChild(imgDiv)
			})
		}
	}

	function Form(_form) {
		this.formElement = _form

		this.formElement.addEventListener("submit", (e) => {
			e.preventDefault()
			let form = e.target.closest("form")
			if (form.checkValidity()) {
				makeCall("POST", "AppendCategory", form, (x) => {
					if (x.readyState == 4) {
						let msg = x.responseText
						if (x.status == 200) {
							treeDiv.show();
						}
						else {
							alert(msg)
						}
						form.reset()
					}
				})
			}
		})

		this.update = (hierarchicalTree) => {
			let linearizedTree = []
			this.linearize(hierarchicalTree, linearizedTree)
			let select = this.formElement.getElementsByTagName("select")[0]
			select.innerHTML = ""
			linearizedTree.forEach((node) => {
				if (node.children.length < 9) {
					let option = document.createElement("option")
					option.value = node.id
					option.innerHTML = node.hierarchy + ", " + node.name
					select.appendChild(option)
				}
			})
			// Add the first option: "no father", with value -1
			if (hierarchicalTree.length < 9) {
				let option = document.createElement("option")
				option.value = -1
				option.innerHTML = "nessun padre"
				select.insertBefore(option, select.firstChild)
			}
			select.selectedIndex = 0;
		}

		this.linearize = (hierarchicalTree, linearizedTree) => {
			hierarchicalTree.forEach((node) => {
				linearizedTree.push(node)
				if (node.children.length != 0)
					this.linearize(node.children, linearizedTree)
			})
		}



	}

	function PageOrchestrator() {
		//var alertContainer = document.getElementById("alertContainer")

		this.start = () => {
			treeDiv = new Tree(document.getElementById("treeContainer"), document.getElementById("alertDiv"))
			treeDiv.show()
			imgContainer = new ImgContainer(document.getElementById("images"))
			
			formContainer = new Form(document.getElementById("newCategoryForm"))

			document.getElementById("logout").onclick = () => {
				window.sessionStorage.removeItem('currentUser')
				makeCall("GET", "Logout", null, (req) => {
					if (req.readyState == 4) {
						var msg = req.responseText
						if (req.status == 202) {
							window.location.href = "index.html"
						} else { }
					}
				})
			}
		}

		this.refresh = () => {
			//alertContainer.innerHTML = ""
			imgContainer.reset()
			treeDiv.reset()
			//formContainer.reset()
		}
	}
}