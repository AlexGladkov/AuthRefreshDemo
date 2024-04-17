package ru.leroymerlin.mobile.auth.data.data_source.keychain

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import platform.darwin.noErr

class KeychainHelper(private val serviceName: String) {

    private enum class Operation { Set, Get, Update, Delete }

    @Suppress("CastToNullableType")
    fun string(forKey: String): String? {
        data(forKey)?.let { data ->
            return NSString.create(data, NSUTF8StringEncoding) as String?
        } ?: kotlin.run {
            return null
        }
    }

    fun set(key: String, value: String): Boolean {
        val convertedData = (value as? NSString)?.dataUsingEncoding(NSUTF8StringEncoding) ?: return false
        return set(key = key, value = convertedData)
    }

    @OptIn(ExperimentalForeignApi::class)
    @ExperimentalUnsignedTypes
    private fun set(key: String, value: NSData): Boolean {
        val capacity: CFIndex = 4
        val query = CFDictionaryCreateMutable(
            null,
            capacity,
            null,
            null,
        )
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
        CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(value))
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(serviceName))

        return if (existsObject(key)) {
            update(key, value)
        } else {
            perform(Operation.Set, query)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    @ExperimentalUnsignedTypes
    private fun existsObject(forKey: String): Boolean {
        val capacity: CFIndex = 4
        val query = CFDictionaryCreateMutable(
            null,
            capacity,
            null,
            null,
        )

        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(forKey))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanFalse)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(serviceName))

        memScoped {
            val result = alloc<CFTypeRefVar>()
            if (perform(Operation.Get, query, result, verbose = false)) {
                return true
            }
        }

        return false
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun data(forKey: String): NSData? {
        val query = CFDictionaryCreateMutable(null, 4, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(forKey))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        memScoped {
            val result = alloc<CFTypeRefVar>()
            if (perform(Operation.Get, query, result)) {
                return CFBridgingRelease(result.value) as NSData
            }
        }

        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun update(forKey: String, value: NSData): Boolean {
        val query = CFDictionaryCreateMutable(null, 3, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(forKey))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanFalse)

        val updateQuery = CFDictionaryCreateMutable(null, 1, null, null)
        CFDictionaryAddValue(updateQuery, kSecValueData, CFBridgingRetain(value))

        return perform(Operation.Update, query, updateQuery = updateQuery)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun deleteObject(forKey: String): Boolean {
        val query = CFDictionaryCreateMutable(null, 3, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(forKey))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanFalse)
        return perform(Operation.Delete, query = query)
    }

    @OptIn(ExperimentalForeignApi::class)
    @ExperimentalUnsignedTypes
    private fun perform(
        operation: Operation,
        query: CFMutableDictionaryRef?,
        result: CFTypeRefVar? = null,
        updateQuery: CFDictionaryRef? = null,
        verbose: Boolean? = true,
    ): Boolean {

        val status = when (operation) {
            Operation.Set -> SecItemAdd(query, result?.ptr)
            Operation.Get -> SecItemCopyMatching(query, result?.ptr)
            Operation.Update -> SecItemUpdate(query, updateQuery)
            Operation.Delete -> SecItemDelete(query)
        }

        return status.toUInt() == noErr
    }
}