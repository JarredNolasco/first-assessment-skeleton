export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents, timestamp }) {
    this.username = username
    this.command = command
    this.contents = contents
    this.timestamp = timestamp
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      timestamp: this.timestamp
    })
  }

  toString () {
    if(this.command === 'echo')
    return `${this.timestamp} <${this.username}> (echo): ${this.contents}`

    if(this.command === 'broadcast')
    return `${this.timestamp} <${this.username}> (all): ${this.contents}`

    if(this.command === 'connect')
    return `${this.timestamp} <${this.username}> has connected`

    if(this.command === 'disconnect')
    return this.timestamp

    if(this.command === 'users')
    return `${this.timestamp} currently connected users : <${this.contents}>`

    if(this.command === '@')
    return `${this.timestamp} <${this.username}> (whisper): ${this.contents}`



  }
}
