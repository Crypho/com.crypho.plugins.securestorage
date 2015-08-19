#import <Foundation/Foundation.h>
#import <Security/Security.h>
#import "SecureStorage.h"
#import <Cordova/CDV.h>
#import "SSKeychain.h"

@implementation SecureStorage

@synthesize callbackId;

- (void)get:(CDVInvokedUrlCommand*)command
{
    NSString *service = [command argumentAtIndex:0];
    NSString *key = [command argumentAtIndex:1];
    NSError *error;

    self.callbackId = command.callbackId;

    SSKeychainQuery *query = [[SSKeychainQuery alloc] init];
    query.service = service;
    query.account = key;

    if ([query fetch:&error]) {
        [self successWithMessage: query.password];
    } else {
        [self failWithMessage: @"Failure in SecureStorage.get()" withError: error];
    }
}

- (void)set:(CDVInvokedUrlCommand*)command
{
    NSString *service = [command argumentAtIndex:0];
    NSString *key = [command argumentAtIndex:1];
    NSString *value = [command argumentAtIndex:2];
    NSError *error;

    self.callbackId = command.callbackId;

    [SSKeychain setAccessibilityType: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly];
    
    SSKeychainQuery *query = [[SSKeychainQuery alloc] init];
    
    query.service = service;
    query.account = key;
    query.password = value;

    if ([query save:&error]) {
        [self successWithMessage: key];
    } else {
        [self failWithMessage: @"Failure in SecureStorage.set()" withError: error];
    }
}

- (void)remove:(CDVInvokedUrlCommand*)command
{
    NSString *service = [command argumentAtIndex:0];
    NSString *key = [command argumentAtIndex:1];
    NSError *error;

    self.callbackId = command.callbackId;

    SSKeychainQuery *query = [[SSKeychainQuery alloc] init];
    query.service = service;
    query.account = key;

    if ([query deleteItem:&error]) {
        [self successWithMessage: key];
    } else {
        [self failWithMessage: @"Failure in SecureStorage.get()" withError: error];
    }
}

-(void)successWithMessage:(NSString *)message
{
    if (self.callbackId != nil)
    {
        CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
    }
}

-(void)failWithMessage:(NSString *)message withError:(NSError *)error
{
    NSString        *errorMessage = (error) ? [NSString stringWithFormat:@"%@ - %@", message, [error localizedDescription]] : message;
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];

    [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
}

@end
