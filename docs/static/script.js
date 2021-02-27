!function() {
    const Utils = {
        parseTwemoji: e => twemoji.parse(e),
        directionReveal: () => {
            const containers = document.querySelectorAll(".direction-reveal");

            function getDirection(e, item) {
                const w = item.offsetWidth;
                const h = item.offsetHeight;
                const position = getPosition(item);

                const x = (e.pageX - position.x - (w / 2) * (w > h ? (h / w) : 1));
                const y = (e.pageY - position.y - (h / 2) * (h > w ? (w / h) : 1));
                switch (Math.round(Math.atan2(y, x) / 1.57079633 + 5) % 4) {
                    case 1:
                        return "right";
                    case 2:
                        return "bottom";
                    case 3:
                        return "left";
                    default:
                        return "top";
                }
            }

            function getPosition(el) {
                let xPos = 0;
                let yPos = 0;

                while (el) {
                    xPos += (el.offsetLeft + el.clientLeft);
                    yPos += (el.offsetTop + el.clientTop);

                    el = el.offsetParent;
                }
                return {x: xPos, y: yPos};
            }

            function addClass(e, state) {
                const target = e.currentTarget;
                const directionString = getDirection(e, target);

                target.className = target.className.split(" ").filter(cssClass => !cssClass.startsWith("slide")).join(" ");
                target.classList.add(`slide--${state}-${directionString}`);
            }

            function bindEvents(containerItem) {
                const items = containerItem.querySelectorAll(".direction-reveal__card");

                for (const item of items) {
                    for (const e of ["mouseenter", "focus"]) {
                        item.addEventListener(e, e => {
                            addClass(e, "in");
                        });
                    }

                    for (const e of ["mouseleave", "blur"]) {
                        item.addEventListener(e, e => {
                            addClass(e, "out");
                        });
                    }
                }
            }

            for (const containerItem of containers) {
                bindEvents(containerItem);
            }
        },
        infiniteScroll: (element, options) => {
            return new InfiniteScroll(`#${element.id}`, options);
        },
        tooltip: (element, options) => {
            return new Tooltip(element, options);
        },
        modal: (element, options) => {
            return new Modal(element, options);
        }
    };

    const Container = {
        element: document.getElementById("container-pic"),

        clear: () => Container.removeAllChildNodes(Container.element),
        removeAllChildNodes: element => {
            while (element.firstChild) {
                element.firstChild.remove();
            }
        }
    };

    const App = {
        mediaBaseUrl: "https://stella-api.starry.blue/media/",
        mobileWarningArea: document.getElementById("mobile-warning"),
        isMobile: navigator.userAgent.includes("iPhone OS") || navigator.userAgent.includes("Android") || navigator.userAgent.includes("Mobile"),
        countEntriesArea: document.getElementById("count-entries"),
        editorModal: document.getElementById("editor"),

        infiniteScroll: null,
        initializeInfiniteScroll: () => {
            App.infiniteScroll = Utils.infiniteScroll(Container.element, {
                path: function () {
                    const parameters = Settings.parameters();
                    parameters.page = this.pageIndex - 1;
                    parameters.count = 10;
                    const query = API.buildParameterString(parameters);

                    return `https://stella-api.starry.blue/query${query !== null ? "?" + query : ""}`;
                },
                responseType: "text",
                status: ".page-load-status",
                history: false
            });

            App.infiniteScroll.on("load", response => {
                JSON.parse(response).result.map(t => (new PicElementBuilder(t)).build());

                const items = Container.element.querySelectorAll(".direction-reveal__card");
                App.infiniteScroll.appendItems(items);
                Utils.directionReveal();
                API.summary().then(t => {
                    App.countEntriesArea.innerHTML = `現在 <strong>${t.entries}</strong> 件のエントリー / <strong>${t.media}</strong> 個のメディア が登録されています。`;
                });
            });

            App.infiniteScroll.loadNextPage();
        },
        resetInfiniteScroll: () => {
            if (App.infiniteScroll !== null) {
                Container.clear();
                App.infiniteScroll.destroy();
            }

            App.initializeInfiniteScroll();
        },
        sensitiveLevelDescription: level => {
            switch (level) {
                case "1":
                    return "R-15";
                case "2":
                    return "R-18";
                case "3":
                    return "R-18G";
                default:
                    return "全年齢対象";
            }
        }
    };

    const Settings = {
        elements: {
            title: {
                id: "title-search",
                name: "title",
                default: ""
            },
            description: {
                id: "description-search",
                name: "description",
                default: ""
            },
            tags: {
                id: "tags-search",
                name: "tags",
                default: ""
            },
            author: {
                id: "author-search",
                name: "author",
                default: ""
            },

            createdSince: {
                id: "created-since-search",
                name: "created_since",
                default: ""
            },
            createdUntil: {
                id: "created-until-search",
                name: "created_until",
                default: ""
            },
            addedSince: {
                id: "added-since-search",
                name: "added_since",
                default: ""
            },
            addedUntil: {
                id: "added-until-search",
                name: "added_until",
                default: ""
            },
            updatedSince: {
                id: "added-since-search",
                name: "updated_since",
                default: ""
            },
            updatedUntil: {
                id: "added-until-search",
                name: "updated_until",
                default: ""
            },

            minRating: {
                id: "min-rating-search",
                name: "min_rating",
                default: ""
            },
            maxRating: {
                id: "max-rating-search",
                name: "max_rating",
                default: ""
            },
            minBookmark: {
                id: "min-bookmark-search",
                name: "min_bookmark",
                default: ""
            },
            maxBookmark: {
                id: "max-bookmark-search",
                name: "max_bookmark",
                default: ""
            },
            minView: {
                id: "min-view-search",
                name: "min_view",
                default: ""
            },
            maxView: {
                id: "max-view-search",
                name: "max_view",
                default: ""
            },
            minLike: {
                id: "min-like-search",
                name: "min_like",
                default: ""
            },
            maxLike: {
                id: "max-like-search",
                name: "max_like",
                default: ""
            },
            minRetweet: {
                id: "min-retweet-search",
                name: "min_retweet",
                default: ""
            },
            maxRetweet: {
                id: "max-retweet-search",
                name: "max_retweet",
                default: ""
            },
            minReply: {
                id: "min-reply-search",
                name: "min_reply",
                default: ""
            },
            maxReply: {
                id: "max-reply-search",
                name: "max_reply",
                default: ""
            },

            platform: {
                id: "platform-select",
                name: "platform",
                default: ""
            },
            ext: {
                id: "ext-select",
                name: "ext",
                default: ""
            },

            sensitiveLevel0: {
                id: "sensitive-level-0-checkbox",
                default: true
            },
            sensitiveLevel1: {
                id: "sensitive-level-1-checkbox",
                default: false
            },
            sensitiveLevel2: {
                id: "sensitive-level-2-checkbox",
                default: false
            },
            sensitiveLevel3: {
                id: "sensitive-level-3-checkbox",
                default: false
            },

            sort: {
                id: "sort-select",
                name: "sort",
                default: "manual_updated_descending"
            },
            diff: {
                id: "diff-checkbox",
                default: true
            }
        },
        assignElements: () => {
            for (const key in Settings.elements) {
                const e = Settings.elements[key];
                e.element = document.getElementById(e.id)
            }
        },
        resetButton: document.getElementById("reset-button"),
        refreshButton: document.getElementById("refresh-button"),
        activeSensitiveLevelElements: () => [Settings.elements.sensitiveLevel0, Settings.elements.sensitiveLevel1, Settings.elements.sensitiveLevel2, Settings.elements.sensitiveLevel3].filter(e => e.element.checked),
        sensitiveLevels: () => Settings.activeSensitiveLevelElements().map(e => e.element.value),
        updateCookies: () => {
            const settings = Object.values(Settings.elements).map(e => {
                let value;
                if (e.element.type === "checkbox") {
                    value = e.element.checked;
                } else {
                    value = e.element.value;
                }
                return {[e.id]: value};
            }).reduce((l, e) => Object.assign(l, e));

            settings.theme = Settings.theme.darkButton.disabled ? "dark" : "light";

            Cookies.set("pic-settings", settings);
        },
        loadCookies: () => {
            const settings = Cookies.getJSON("pic-settings");
            if (settings === undefined) {
                return;
            }

            for (const key in Settings.elements) {
                const e = Settings.elements[key];
                if (e.element.type === "checkbox") {
                    e.element.checked = settings[e.id];
                } else {
                    e.element.value = settings[e.id];
                }
            }

            if (settings.theme === "dark") {
                Settings.theme.enableDarkTheme();
            } else {
                Settings.theme.enableLightTheme();
            }
        },
        parameters: () => {
            const params = Object.values(Settings.elements).filter(e => e.name !== undefined).map(e => ({[e.name]: e.element.value})).reduce((l, e) => Object.assign(l, e));
            params.sensitive_levels = Settings.sensitiveLevels().join(",");

            return params;
        },
        setEventListeners: () => {
            Settings.resetButton.onclick = Settings.reset;
            Settings.refreshButton.onclick = Settings.onCheckboxSettingChanged;
            Settings.theme.darkButton.onclick = () => {
                Settings.theme.enableDarkTheme();
                Settings.updateCookies();
            };
            Settings.theme.lightButton.onclick = () => {
                Settings.theme.enableLightTheme();
                Settings.updateCookies();
            };

            for (const key in Settings.elements) {
                const element = Settings.elements[key].element;
                switch (element.type) {
                    case "text":
                        element.oninput = Settings.onSettingChanged;
                        break;
                    case "checkbox":
                        element.onchange = Settings.onCheckboxSettingChanged;
                        break;
                    default:
                        element.onchange = Settings.onSettingChanged;
                }
            }
        },
        onSettingChanged: () => {
            App.resetInfiniteScroll();
            Settings.updateCookies();
        },
        onCheckboxSettingChanged: () => {
            const filtered = Settings.activeSensitiveLevelElements();
            if (filtered.length === 1) {
                filtered[0].element.disabled = true
            } else {
                for (const e of filtered) {
                    e.element.disabled = false
                }
            }

            Settings.onSettingChanged();
        },
        reset: () => {
            for (const key in Settings.elements) {
                const config = Settings.elements[key];
                switch (config.element.type) {
                    case "checkbox":
                        config.element.checked = config.default;
                        break;
                    default:
                        config.element.value = config.default;
                }
            }

            Settings.onCheckboxSettingChanged();
        },
        theme: {
            darkButton: document.getElementById("dark-button"),
            lightButton: document.getElementById("light-button"),
            applyDarkCss: () => {
                const link = document.createElement("link");
                link.href = "/static/dark-theme.css";
                link.rel = "stylesheet";
                link.classList.add("dark");
                document.head.appendChild(link);
            },
            removeDarkCss: () => {
                const dark = document.querySelector("link[class=\"dark\"]");
                if (dark) {
                    dark.remove();
                };
            },
            enableDarkTheme: () => {
                Settings.theme.applyDarkCss();
                Settings.theme.lightButton.disabled = false;
                Settings.theme.darkButton.disabled = true;
                Settings.theme.lightButton.style = "";
                Settings.theme.darkButton.style = "display: none;";
            },
            enableLightTheme: () => {
                Settings.theme.removeDarkCss();
                Settings.theme.darkButton.disabled = false;
                Settings.theme.lightButton.disabled = true;
                Settings.theme.darkButton.style = "";
                Settings.theme.lightButton.style = "display: none;";
            }
        }
    };

    class PicElementBuilder {
        constructor(pic) {
            this.pic = pic;
        }

        buildEach(media) {
            const mediaItem = new PicMediaItemBuilder(this.pic, media);
            const a = mediaItem.build();
            Container.element.appendChild(a);

            const overlay = new PicOverlayBuilder(this.pic, media);
            a.appendChild(overlay.build());
        }

        build() {
            for (const media of this.pic.media) {
                if (!Settings.elements.diff.element.checked && media.index > 0) {
                    break;
                }

                this.buildEach(media)
            }

            for (const element of Array.from(document.querySelectorAll(".overlay-description a"))) {
                element.target = "_blank";
                if (element.innerText.length > 30) {
                    element.innerText = element.innerText.slice(0, 30);
                }
            }
        }
    }

    class PicMediaItemBuilder {
        constructor(pic, media) {
            this.pic = pic;
            this.media = media;
        }

        createMediaItem() {
            const a = document.createElement("a");
            a.classList.add("direction-reveal__card", "media-item");
            a.setAttribute("data-id", this.pic.id);
            return a;
        }

        createMediaItemImage(a) {
            if (this.media.ext === "mp4" || this.media.ext === "m3u8") {
                const video = document.createElement("video");
                a.appendChild(video);
                video.classList.add("media-item__image");
                video.loop = true;
                video.autoplay = true;
                video.muted = true;
                video.setAttribute("playsinline", "true");
                video.src = `${App.mediaBaseUrl}${this.media.filename}`;
                video.alt = `Video by ${this.pic.author.name}`;
                return video;
            } else {
                const img = new Image();
                a.appendChild(img);
                img.classList.add("media-item__image");
                img.src = `${App.mediaBaseUrl}${this.media.filename}`;
                img.alt = `Picture by ${this.pic.author.name}`;
                return img;
            }
        }

        build() {
            const a = this.createMediaItem();
            const media = this.createMediaItemImage(a);
            a.appendChild(media);
            return a;
        }
    }

    class PicOverlayBuilder {
        constructor(pic, media) {
            this.pic = pic;
            this.media = media;
            this.overlayDiv = document.createElement("div");
            this.overlayDiv.classList.add("direction-reveal__overlay", "direction-reveal__anim--in", "overlay");
        }

        createTitle() {
            const title = document.createElement("h4");
            this.overlayDiv.appendChild(title);
            title.classList.add("direction-reveal__title", "overlay-title");
            title.innerText = this.pic.title;
            if (this.pic.media.length > 1) {
                title.innerText += ` #${this.media.index + 1}`;
            }
            if (this.pic.sensitive_level > 0) {
                title.innerHTML = `<i class="fas fa-exclamation-circle" title="NSFW"></i> ${title.innerText}`;
            }
            return title;
        }

        createCloseButton() {
            const closeButton = document.createElement("button");
            closeButton.classList.add("btn", "btn-dark", "btn-sm", "overlay-close-button");
            closeButton.setAttribute("type", "button");
            closeButton.onclick = () => {
                this.overlayDiv.parentElement.className = this.overlayDiv.parentElement.className.replace("--in", "--out");
            };
            closeButton.innerHTML += '<i class="far fa-times-circle"></i>';
            return closeButton;
        }

        createAuthor() {
            const author = document.createElement("p");
            author.classList.add("direction-reveal__text", "overlay-author");
            if (this.pic.author.username !== null) {
                author.innerHTML = `${this.pic.author.name} @${this.pic.author.username} (<a href="${this.pic.author.url}" target="_blank">${this.pic.platform}</a>)`;
            } else {
                author.innerHTML = `${this.pic.author.name} (<a href="${this.pic.author.url}" target="_blank">${this.pic.platform}</a>)`;
            }

            if (this.pic.user !== null) {
                author.setAttribute("title", `追加したユーザ: ${this.pic.user}`);
                Utils.tooltip(author, {
                    placement: "right",
                    delay: 200
                });
            }
            return author;
        }

        createRate() {
            const rate = document.createElement("p");
            rate.classList.add("direction-reveal__text", "overlay-rate");

            if (this.pic.popularity.reply !== null) {
                const reply = document.createElement("span");
                rate.appendChild(reply);
                reply.classList.add("badge", "badge-pill", "badge-light", "overlay-rate-badge");
                reply.innerHTML = `<i class="fas fa-reply"></i> ${this.pic.popularity.reply}`;
            }
            if (this.pic.popularity.retweet !== null) {
                const retweet = document.createElement("span");
                rate.appendChild(retweet);
                retweet.classList.add("badge", "badge-pill", "badge-light", "overlay-rate-badge");
                retweet.innerHTML = `<i class="fas fa-retweet"></i> ${this.pic.popularity.retweet}`;
            }
            if (this.pic.popularity.like !== null) {
                const like = document.createElement("span");
                rate.appendChild(like);
                like.classList.add("badge", "badge-pill", "badge-light", "overlay-rate-badge");
                like.innerHTML = `<i class="fas fa-heart"></i> ${this.pic.popularity.like}`;
            }
            if (this.pic.popularity.bookmark !== null) {
                const bookmark = document.createElement("span");
                rate.appendChild(bookmark);
                bookmark.classList.add("badge", "badge-pill", "badge-light", "overlay-rate-badge");
                bookmark.innerHTML = `<i class="fas fa-bookmark"></i> ${this.pic.popularity.bookmark}`;
            }
            if (this.pic.popularity.view !== null) {
                const view = document.createElement("span");
                rate.appendChild(view);
                view.classList.add("badge", "badge-pill", "badge-light", "overlay-rate-badge");
                view.innerHTML = `<i class="far fa-eye"></i> ${this.pic.popularity.view}`;
            }

            return rate;
        }

        createTags() {
            const tags = document.createElement("p");
            tags.classList.add("direction-reveal__text", "overlay-tags");
            for (const tag of this.pic.tags) {
                const tagElement = PicOverlayBuilder.createTagEach(tag);
                tags.appendChild(tagElement);
            }
            return tags;
        }

        static createTagEach(tag) {
            const tagElement = document.createElement("a");
            tagElement.classList.add("overlay-tag");
            tagElement.href = "javascript:void(0);";
            tagElement.onclick = () => {
                Settings.elements.tags.element.value = tag.value;
                App.resetInfiniteScroll();
            };
            tagElement.setAttribute("data-tag", tag.value);
            if (tag.locked) {
                tagElement.innerHTML = `<span class="badge badge-primary overlay-badge-tag"><i class="fas fa-lock"></i> ${tag.value}</span>`;
            } else {
                tagElement.innerHTML = `<span class="badge badge-info overlay-badge-tag"><i class="fas fa-tag"></i> ${tag.value}</span>`;
            }
            return tagElement;
        }

        createEdit() {
            const edit = document.createElement("a");
            edit.classList.add("overlay-tag");
            edit.href = "javascript:void(0);";
            edit.innerHTML = `<span class="badge badge-secondary overlay-badge-edit-tag"><i class="fas fa-pencil-alt"></i> 編集</span>`;
            edit.onclick = () => {
                const modal = new PicEditorModalBuilder(this.pic, this.media);
                modal.build();
            };
            return edit;
        }

        createDescription() {
            const description = document.createElement("p");
            description.classList.add("direction-reveal__text", "overlay-description");
            description.innerHTML = this.pic.description;
            return description;
        }

        build() {
            const title = this.createTitle();

            if (App.isMobile) {
                const closeButton = this.createCloseButton();
                title.appendChild(closeButton);
            }

            const author = this.createAuthor();
            this.overlayDiv.appendChild(author);

            this.overlayDiv.appendChild(document.createElement("hr"));

            const rate = this.createRate();
            this.overlayDiv.appendChild(rate);

            this.overlayDiv.appendChild(document.createElement("hr"));

            const tags = this.createTags();
            this.overlayDiv.appendChild(tags);

            const editTag = this.createEdit();
            tags.appendChild(editTag);

            this.overlayDiv.appendChild(document.createElement("hr"));

            const description = this.createDescription();
            this.overlayDiv.appendChild(description);
            Utils.parseTwemoji(this.overlayDiv);

            const toolboxDiv = new PicOverlayToolboxBuilder(this.pic, this.media);
            this.overlayDiv.appendChild(toolboxDiv.build());

            return this.overlayDiv;
        }
    }

    class PicOverlayToolboxBuilder {
        constructor(pic, media) {
            this.pic = pic;
            this.media = media;
            this.toolboxDiv = document.createElement("div");
            this.toolboxDiv.classList.add("overlay-toolbox");
        }

        createTimestamp() {
            const timestamp = document.createElement("p");
            timestamp.classList.add("direction-reveal__text", "overlay-timestamp");
            timestamp.innerText = `投稿日時: ${(new Date(this.pic.timestamp.created)).toLocaleString()}`;

            timestamp.setAttribute("title", `追加日時: ${(new Date(this.pic.timestamp.added)).toLocaleString()}<br>ユーザ更新日時: ${(new Date(this.pic.timestamp.manual_updated)).toLocaleString()}<br>システム更新日時: ${(new Date(this.pic.timestamp.auto_updated)).toLocaleString()}`);
            Utils.tooltip(timestamp, {
                placement: "right",
                delay: 200
            });

            return timestamp;
        }

        createOriginalLinkButton() {
            const originalLinkButton = document.createElement("button");
            originalLinkButton.classList.add("btn", "btn-info", "btn-sm", "overlay-button");
            originalLinkButton.setAttribute("type", "button");
            originalLinkButton.onclick = () => window.open(this.pic.url);
            originalLinkButton.innerHTML += `<i class="fas fa-external-link-alt"></i> ${this.pic.platform} で見る`;
            return originalLinkButton;
        }

        createRawMediaLinkButton() {
            const rawMediaButton = document.createElement("button");
            rawMediaButton.classList.add("btn", "btn-success", "btn-sm", "overlay-button");
            rawMediaButton.setAttribute("type", "button");
            rawMediaButton.onclick = () => window.open(`${App.mediaBaseUrl}${this.media.filename}`);
            rawMediaButton.innerHTML += '<i class="far fa-file-image"></i> 新しいタブで開く';
            return rawMediaButton;
        }

        createRefreshButton() {
            const refreshButton = document.createElement("button");
            refreshButton.classList.add("btn", "btn-light", "btn-sm", "overlay-button");
            refreshButton.setAttribute("type", "button");
            refreshButton.onclick = () => {
                API.refreshEntry(this.pic).then(() => {
                    App.resetInfiniteScroll();
                });
            };
            refreshButton.innerHTML += '<i class="fas fa-database"></i> データを再取得する';
            return refreshButton;
        }

        build() {
            const timestamp = this.createTimestamp();
            this.toolboxDiv.appendChild(timestamp);

            const originalLinkButton = this.createOriginalLinkButton();
            this.toolboxDiv.appendChild(originalLinkButton);

            const rawMediaLinkButton = this.createRawMediaLinkButton();
            this.toolboxDiv.appendChild(rawMediaLinkButton);

            const refreshButton = this.createRefreshButton();
            this.toolboxDiv.appendChild(refreshButton);

            return this.toolboxDiv;
        }
    }

    class PicEditorModalBuilder {
        constructor(pic, media) {
            this.pic = pic;
            this.media = media;

            this.tagRelationalResult = null;
            this.tagSearchInput = null;
            this.tagSearchResult = null;
            this.tagCurrentResult = null;
            this.tagRandomRefreshButton = null;

            this.sensitiveLevelRange = null;
            this.sensitiveLevelDescription = null;
            this.sensitiveLevelButton = null;
        }

        createHeader() {
            const header = document.createElement("div");
            header.classList.add("modal-header");
            return header;
        }

        createTitle() {
            const h4 = document.createElement("h4");
            h4.classList.add("modal-title");
            h4.innerText = `"${this.pic.title}" を編集`;
            return h4;
        }

        createCloseButton() {
            const button = document.createElement("button");
            button.setAttribute("type", "button");
            button.classList.add("close");
            button.setAttribute("data-dismiss", "modal");
            button.innerHTML = '<span aria-hidden="true">&times;</span>';
            return button;
        }

        createBody() {
            const body = document.createElement("div");
            body.classList.add("modal-body");
            return body;
        }

        createAddTag() {
            const div = document.createElement("div");
            div.classList.add("add-tags");

            const title = document.createElement("h5");
            div.appendChild(title);
            title.classList.add("editor-modal-title");
            title.innerHTML = '<i class="fas fa-tags"></i>タグ追加';

            const wrapper = document.createElement("div");
            div.appendChild(wrapper);
            wrapper.innerHTML = '<p><i class="fas fa-search"></i> 既存のタグを検索</p>';
            const input2 = document.createElement("div");
            wrapper.appendChild(input2);
            input2.classList.add("input-group", "mb-3");
            input2.innerHTML = '<input type="text" class="form-control input-text-search-tag" placeholder="タグを入力...">';
            const searchResult = document.createElement("div");
            wrapper.appendChild(searchResult);
            searchResult.classList.add("tag-search-result");

            const wrapper2 = document.createElement("div");
            div.appendChild(wrapper2);
            wrapper2.innerHTML = '<p><i class="fas fa-random"></i> 関連タグから追加<span class="controls"><a class="btn btn-outline-success btn-sm tag-random-refresh-button">更新</a></span></p>';
            const randomResult = document.createElement("div");
            wrapper2.appendChild(randomResult);
            randomResult.classList.add("tag-random-result");

            return div;
        }

        createAddTagEach(tagValue) {
            const a = document.createElement("a");
            a.classList.add("overlay-tag");
            a.href = "javascript:void(0);";

            const badge = document.createElement("span");
            a.appendChild(badge);
            badge.classList.add("badge", "badge-success", "overlay-badge-tag", "add-tag");
            badge.setAttribute("data-tag", tagValue);
            badge.innerHTML = `<i class="fas fa-tag"></i> ${tagValue}`;
            return a;
        }

        createNewTag(tagValue) {
            const a = document.createElement("a");
            a.classList.add("overlay-tag");
            a.href = "javascript:void(0);";

            const badge = document.createElement("span");
            a.appendChild(badge);
            badge.classList.add("badge", "badge-info", "overlay-badge-tag", "add-tag");
            badge.setAttribute("data-tag", tagValue);
            badge.innerHTML = `<i class="fas fa-pencil-alt"></i> "${tagValue}" を新しく追加する`;
            return a;
        }

        createDeleteTag() {
            const div = document.createElement("div");
            div.classList.add("delete-tags");

            const title = document.createElement("h5");
            div.appendChild(title);
            title.classList.add("editor-modal-title");
            title.innerHTML = '<i class="fas fa-eraser"></i>タグ削除';

            const result = document.createElement("div");
            div.appendChild(result);
            result.classList.add("tag-current-result");

            return div;
        }

        createDeleteTagEach(tag) {
            const a = document.createElement("a");
            a.classList.add("overlay-tag");
            a.href = "javascript:void(0);";

            const badge = document.createElement("span");
            a.appendChild(badge);
            if (tag.locked) {
                badge.classList.add("badge", "badge-secondary", "overlay-badge-tag", "locked-tag");
                badge.innerHTML = `<i class="fas fa-lock"></i> ${tag.value}`;
            } else {
                badge.classList.add("badge", "badge-danger", "overlay-badge-tag", "delete-tag");
                badge.innerHTML = `<i class="fas fa-tag"></i> ${tag.value}`;
            }
            badge.setAttribute("data-tag", tag.value);
            return a;
        }

        createEditSensitiveLevel() {
            const div = document.createElement("div");
            div.classList.add("update-sensitive-level");

            const title = document.createElement("h5");
            div.appendChild(title);
            title.classList.add("editor-modal-title");
            title.innerHTML = '<i class="fas fa-wrench"></i>コンテンツレベル 変更';

            const inputGroup = document.createElement("div");
            div.appendChild(inputGroup);
            inputGroup.classList.add("input-group");

            const input = document.createElement("input");
            inputGroup.appendChild(input);
            input.classList.add("custom-range", "sensitive-level-range", "form-control");
            input.setAttribute("type", "range");
            input.setAttribute("min", "0");
            input.setAttribute("max", "3");
            input.setAttribute("value", `${this.pic.sensitive_level}`);

            const buttonWrapper = document.createElement("div");
            inputGroup.appendChild(buttonWrapper);
            buttonWrapper.classList.add("input-group-append");
            const button = document.createElement("button");
            buttonWrapper.appendChild(button);
            button.setAttribute("type", "button");
            button.classList.add("btn", "btn-outline-primary", "sensitive-level-button");
            button.innerText = "変更";

            const p2 = document.createElement("label");
            div.appendChild(p2);
            p2.classList.add("sensitive-level-description");

            return div;
        }

        createMedia() {
            if (this.media.ext === "mp4" || this.media.ext === "m3u8") {
                const video = document.createElement("video");
                video.classList.add("media-item__image");
                video.loop = true;
                video.autoplay = true;
                video.muted = true;
                video.setAttribute("playsinline", "true");
                video.src = `${App.mediaBaseUrl}${this.media.filename}`;
                return video;
            } else {
                const img = new Image();
                img.classList.add("media-item__image");
                img.src = `${App.mediaBaseUrl}${this.media.filename}`;
                return img;
            }
        }

        createFooter() {
            const footer = document.createElement("div");
            footer.classList.add("modal-footer");
            footer.innerHTML = '<button type="button" class="btn btn-default" data-dismiss="modal">閉じる</button>';
            return footer;
        }

        createContent() {
            const wrapper = document.createElement("div");

            const header = this.createHeader();
            wrapper.appendChild(header);

            const title = this.createTitle();
            header.appendChild(title);

            const closeButton = this.createCloseButton();
            header.appendChild(closeButton);

            const body = this.createBody();
            wrapper.appendChild(body);

            const row = document.createElement("div");
            body.appendChild(row);
            row.classList.add("row");

            const col1 = document.createElement("div");
            row.appendChild(col1);
            col1.classList.add("col-md-6");

            col1.appendChild(this.createAddTag());

            col1.appendChild(document.createElement("hr"));

            col1.appendChild(this.createDeleteTag());

            col1.appendChild(document.createElement("hr"));

            col1.appendChild(this.createEditSensitiveLevel());

            const col2 = document.createElement("div");
            row.appendChild(col2);
            col2.classList.add("col-md-6");

            col2.appendChild(this.createMedia());

            const footer = this.createFooter();
            wrapper.appendChild(footer);

            return wrapper.innerHTML;
        }

        static fadeIn(element, ms) {
            element.style = "opacity: 0; filter: alpha(opacity=0);";

            let opacity = 0;
            const timer = setInterval(() => {
                opacity += 50 / ms;
                if (opacity >= 1) {
                    clearInterval(timer);
                    opacity = 1;
                }
                element.style = `opacity: ${opacity}; filter: alpha(opacity=${opacity * 100});`;
            }, 50);
        }

        static fadeOut(element, ms) {
            let opacity = 1;
            const timer = setInterval(function () {
                opacity -= 50 / ms;
                if (opacity <= 0) {
                    clearInterval(timer);
                    opacity = 0;
                    element.style.display = "none";
                    element.style.visibility = "hidden";
                }
                element.style.opacity = opacity;
                element.style.filter = "alpha(opacity=" + opacity * 100 + ")";
            }, 50);
        }

        showInfoAlert(text) {
            const alert = document.createElement("div");
            App.editorModal.querySelector(".modal-body").insertAdjacentElement("afterbegin", alert);
            alert.classList.add("alert", "alert-info", "editor-info-alert");
            alert.setAttribute("role", "alert");
            alert.innerText = text;
            PicEditorModalBuilder.fadeIn(alert, 500);
            setTimeout(() => {
                PicEditorModalBuilder.fadeOut(alert, 500);
                alert.remove();
            }, 10000);
        }

        showErrorAlert(text) {
            const alert = document.createElement("div");
            App.editorModal.querySelector(".modal-body").insertAdjacentElement("afterbegin", alert);
            alert.classList.add("alert", "alert-danger", "editor-danger-alert");
            alert.setAttribute("role", "alert");
            alert.innerText = text;
            PicEditorModalBuilder.fadeIn(alert, 500);
            setTimeout(() => {
                PicEditorModalBuilder.fadeOut(alert, 500);
                alert.remove();
            }, 10000);
        }

        updateRelationalTags() {
            API.relationalTags(this.pic).then(t => {
                Container.removeAllChildNodes(this.tagRelationalResult);

                for (const tag of t.tags) {
                    if (this.pic.tags.filter(e => e.value === tag).length > 0) {
                        continue;
                    }

                    this.tagRelationalResult.appendChild(this.createAddTagEach(tag));
                }
            }).catch(e => {
                console.error(e);
                this.showErrorAlert(`関連タグの取得に失敗しました。`);
            });
        }

        updateSearchTags(name) {
            if (name.length === 0) {
                Container.removeAllChildNodes(this.tagSearchResult);
                return;
            }

            API.searchTags(this.pic, name).then(t => {
                Container.removeAllChildNodes(this.tagSearchResult);

                for (const tag of t.tags) {
                    if (this.pic.tags.filter(e => e.value === tag).length > 0) {
                        continue;
                    }

                    this.tagSearchResult.appendChild(this.createAddTagEach(tag));
                }
            }).catch(e => {
                console.error(e);
                this.showErrorAlert(`タグの検索に失敗しました。`);
            }).then(() => {
                if (this.pic.tags.filter(e => e.value === name).length > 0 || this.tagSearchResult.querySelector(`.add-tag[data-tag=\"${name}\"]`) !== null) {
                    return;
                }

                this.tagSearchResult.appendChild(this.createNewTag(name));
            });
        }

        updateSensitiveLevel(level) {
            API.updateSensitiveLevel(this.pic, level).then(() => {
                this.sensitiveLevelButton.disabled = true;
                this.showInfoAlert(`コンテンツレベル を ${App.sensitiveLevelDescription(level)} に変更しました。`);
            }).catch(e => {
                console.error(e);
                this.showErrorAlert(`コンテンツレベルの変更に失敗しました。`);
            });
        }

        updateSensitiveLevelDisplay(level) {
            this.sensitiveLevelDescription.innerText = App.sensitiveLevelDescription(level);
            this.sensitiveLevelButton.disabled = Number(level) === this.pic.sensitive_level;
            this.pic.sensitive_level = Number(level);
        }

        updateCurrentTags() {
            Container.removeAllChildNodes(this.tagCurrentResult);

            for (const tag of this.pic.tags) {
                this.tagCurrentResult.appendChild(this.createDeleteTagEach(tag));
            }
        }

        addTag(tagValue) {
            API.addTag(this.pic, tagValue).then(() => {
                this.reflectTagAdd(tagValue);
                const span = App.editorModal.querySelector(`.add-tags span[data-tag=\"${tagValue}\"]`);
                if (span !== null) {
                    span.parentElement.remove();
                }
            }).catch(e => {
                console.error(e);
                this.showErrorAlert(`タグ \"${tagValue}\" の追加に失敗しました。`);
            });
        }

        deleteTag(tagValue) {
            API.deleteTag(this.pic, tagValue).then(() => {
                this.reflectTagDelete(tagValue);
                const span = App.editorModal.querySelector(`.tag-current-result span[data-tag=\"${tagValue}\"]`);
                if (span !== null) {
                    span.parentElement.remove();
                }
            }).catch(e => {
                console.error(e);
                this.showErrorAlert(`タグ \"${tagValue}\" の削除に失敗しました。`);
            });
        }

        reflectTagAdd(tagValue) {
            this.pic.tags.push({user: null, locked: false, value: tagValue});
            this.tagCurrentResult.appendChild(this.createDeleteTagEach({user: null, locked: false, value: tagValue}));

            for (const element of Array.from(Container.element.querySelectorAll(`a.media-item[data-id="${this.pic.id}"] .overlay-tags`))) {
                element.insertBefore(PicOverlayBuilder.createTagEach({user: null, locked: false, value: tagValue}), element.lastChild);
            }

            this.showInfoAlert(`タグ \"${tagValue}\" を追加しました。`);
        }

        reflectTagDelete(tagValue) {
            this.pic.tags = this.pic.tags.filter(e => e.value !== tagValue);
            this.tagRelationalResult.appendChild(this.createAddTagEach(tagValue));

            for (const element of Array.from(Container.element.querySelector(`a.media-item[data-id="${this.pic.id}"] a[data-tag=\"${tagValue}\"]`))) {
                element.remove();
            }

            this.showInfoAlert(`タグ \"${tagValue}\" を削除しました。`);
        }

        build() {
            const modal = Utils.modal(App.editorModal, {
                content: this.createContent()
            });
            modal.show();

            this.tagRelationalResult = App.editorModal.querySelector(".tag-random-result");
            this.tagSearchResult = App.editorModal.querySelector(".tag-search-result");
            this.tagSearchInput = App.editorModal.querySelector(".input-text-search-tag");
            this.tagCurrentResult = App.editorModal.querySelector(".tag-current-result");
            this.tagRandomRefreshButton = App.editorModal.querySelector(".tag-random-refresh-button");

            this.sensitiveLevelRange = App.editorModal.querySelector(".sensitive-level-range");
            this.sensitiveLevelDescription = App.editorModal.querySelector(".sensitive-level-description");
            this.sensitiveLevelButton = App.editorModal.querySelector(".sensitive-level-button");

            this.updateRelationalTags();
            this.updateCurrentTags();
            this.updateSensitiveLevelDisplay(this.sensitiveLevelRange.value);

            window.onclick = e => {
                const tag = e.target.getAttribute("data-tag");
                if (e.target.classList.contains("add-tag")) {
                    this.addTag(tag);
                } else if (e.target.classList.contains("delete-tag")) {
                    if (confirm(`タグ "${tag}" を削除してもよろしいですか？`)) {
                        this.deleteTag(tag);
                    }
                }
            };

            this.tagSearchInput.oninput = e => this.updateSearchTags(e.target.value);
            this.tagRandomRefreshButton.onclick = () => this.updateRelationalTags();

            this.sensitiveLevelRange.onchange = e => this.updateSensitiveLevelDisplay(e.target.value);
            this.sensitiveLevelButton.onclick = () => this.updateSensitiveLevel(this.sensitiveLevelRange.value);
        }
    }

    const API = {
        asyncRequest: (method, path, params, data) => {
            return new Promise((resolve, reject) => {
                const xhr = new XMLHttpRequest();
                const query = API.buildParameterString(params);
                xhr.open(method, `https://stella-api.starry.blue${path}${query !== null ? "?" + query : ""}`, true);
                xhr.onload = () => {
                    try {
                        const json = JSON.parse(xhr.responseText);
                        resolve(json);
                    } catch (e) {
                        reject(e);
                    }
                };
                if (data !== null) {
                    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                    xhr.send(API.buildParameterString(data));
                } else {
                    xhr.send();
                }
            });
        },
        buildParameterString: params => {
            if (params === null) {
                return null;
            }

            return Object.entries(params).filter(value => {
                const v = value[1];
                switch (typeof v) {
                    case "string":
                        return v.length !== 0 && v !== "null";
                    default:
                        return true;
                }
            }).map(value => `${encodeURIComponent(value[0])}=${encodeURIComponent(value[1])}`).join("&");
        },
        summary: () => API.asyncRequest("GET", "/summary", null, null),
        refreshEntry: pic => API.asyncRequest("PUT", `/refresh/${pic.id}`, null, null),
        addTag: (pic, tag) => API.asyncRequest("PUT", `/edit/${pic.id}/tag`, null, {tag: tag}),
        deleteTag: (pic, tag) => API.asyncRequest("DELETE", `/edit/${pic.id}/tag`, null, {tag: tag}),
        updateSensitiveLevel: (pic, level) => API.asyncRequest("PATCH", `/edit/${pic.id}/sensitive_level`, null, {sensitive_level: level}),
        relationalTags: pic => API.asyncRequest("GET", "/query/tags", {id: pic.id, sensitive_level: pic.sensitive_level, count: 20}, null),
        searchTags: (pic, name) => API.asyncRequest("GET", "/query/tags", {id: pic.id, name: name, sensitive_level: pic.sensitive_level, count: 30}, null)
    };

    if (App.isMobile) {
        App.mobileWarningArea.style = "";
    }

    window.addEventListener("load", () => {
        quicklink();
    });

    Settings.assignElements();
    Settings.loadCookies();
    App.initializeInfiniteScroll();
    Settings.setEventListeners();
}();
