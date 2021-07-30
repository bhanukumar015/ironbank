package hyperface.cms.kong.dto

class ConsumerObject {

    /** The unique username of the consumer */
    String username

    /** unique ID for the consumer */
    String custom_id

    ConsumerObject(String username) {
        this.username = username
    }

    ConsumerObject(String username, String custom_id) {
        this.username = username
        this.custom_id = custom_id
    }


}
