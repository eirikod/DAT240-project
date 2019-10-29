/**
 * Composed class containing all socket functionality in abstract form
 */
class SocketConnector {
    constructor(socketURL = '/ws-default-connection') {
        this.socket = new SockJS(socketURL);
        this.stomp = Stomp.over(this.socket);
        const _this = this;
        this.stomp.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            for (let listener of _this.stompListenerQueue) {
                _this.stomp.subscribe(listener.destination, listener.callback);
            }
            this.stompListenerQueue = null; // Delete the queue since all listeners have been added!
        });

        this.stompListenerQueue = [];
    }

    /**
     *
     * @param data {object} - Data to be sent to the server
     * @param route {string} - Routing URL
     * @author Alan Rostem
     */
    send(data, route = "/app/home") {
        if (data) {
            if (typeof data === "object") {
                this.stomp.send(route, {}, JSON.stringify(data));
            } else {
                this.stomp.send(route, {}, JSON.stringify({data: data}));
            }
        }
    }

    /**
     * Add a listener to a socket event and do something with the received data
     * @param destination {string} - Destination URL
     * @param callback {function} - Function that takes the data as parameter
     * @author Alan Rostem
     */
    addStompListener(destination, callback) {
        this.stompListenerQueue.push({
            destination: destination,
            callback: data => {
                callback(JSON.parse(data.body).content);
            }
        });
    }

    /**
     * Disconnect the socket from the server
     * @author Alan Rostem
     */
    disconnect() {
        if (this.stomp !== null) {
            this.stomp.disconnect();
        }
        console.log("Disconnected.");
    }
}