/**
 * Composed class containing all socket functionality in abstract form
 */
class SocketConnector {
    constructor(endpoint = '/ws') {
        this.connected = false;
        this.subscriptions = {};
        this.socket = new SockJS(endpoint);
        this.stomp = Stomp.over(this.socket);
        const _this = this;
        this.stomp.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            _this.connected = true;
            for (let listener of _this.stompListenerQueue) {
                _this.subscribe(listener.destination, listener.callback);
            }
            _this.stompListenerQueue = null; // Delete the queue since all listeners have been added!
        });

        this.stompListenerQueue = [];
    }

    /**
     *
     * @param data {object} - Data to be sent to the server
     * @param route {string} - Routing URL
     * @author Alan Rostem
     */
    send(data, route) {
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
        if (!this.connected) {
            this.stompListenerQueue.push({
                destination: destination,
                callback: data => {
                    if (typeof data === "object") {
                        callback(data);
                    } else if (typeof data === "string"){
                        callback(JSON.parse(data.body).content);
                    }
                }
            });
        } else {
            this.subscribe(destination, callback);
        }
    }

    /**
     * TODO: Add docs
     * @param destination
     * @param callback
     */
    subscribe(destination, callback) {
        this.subscriptions[destination] =
            this.stomp.subscribe(destination, data => {
            	console.log(data);
            	if (data){
            		if (data.body){
            			callback(data.body);            			
            		}
            	}
            	else{
            		callback(data);
            	}
            });
        //console.log(destination, this.subscriptions[destination])
        return this.subscriptions[destination];
    }

    /**
     * TODO: Add docs
     * @param destination
     */
    unsubscribe(destination) {
        if (this.subscriptions[destination]) {
            this.subscriptions[destination].unsubscribe();
            delete this.subscriptions[destination];
        }
    }

    onConnect() {

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