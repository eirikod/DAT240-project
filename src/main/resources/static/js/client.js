class Client {
    constructor(socketURL) {
        this.socket = new SockJS(socketURL);
        this.stomp = Stomp.over(this.socket);
        const _this = this;
        this.stomp.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            _this.stomp.subscribe('/topic/greetings', function (greeting) {
                console.log("Got this data:", JSON.parse(greeting.body).content);
            });
        });
    }

    send(data, route = "/app/hello") {
        if (data) {
            if (typeof data === "object") {
                this.stomp.send(route, {}, JSON.stringify(data));
            } else {
                this.stomp.send(route, {}, JSON.stringify({data: data}));
            }
        }
    }

    /**
     * Add a listener to a socket event
     * @param destination {string} - Destination URL
     * @param callback {function} - Function that takes the data as parameter
     */
    addStompListener(destination, callback) {
        this.stomp.subscribe(destination, data => {
            callback(JSON.parse(data.body).content);
        });
    }

    disconnect() {
        if (this.stomp !== null) {
            this.stomp.disconnect();
        }
        console.log("Disconnected.");
    }
}

const client = new Client('/gs-guide-websocket');
