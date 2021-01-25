package users.api.server

import java.io.IOException

class BadRequestException(message: String) : IllegalArgumentException(message)

class ResourceNotFoundException(message: String) : IOException(message)
