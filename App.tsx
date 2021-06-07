/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  Button,
  FlatList,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';
import useQPOS from './src/hooks/useQPOS';
import useQPOSScan from './src/hooks/useQPOSScan';
import useQPOSTrade from './src/hooks/useQPOSTrade';
import {QPOSScanDevice} from './src/types';

const deviceKeyExtractor = (device: QPOSScanDevice) => device.address;

const App = () => {
  const isDarkMode = useColorScheme() === 'dark';
  const {error, connected, connect} = useQPOS();
  const {scanning, devices, scan} = useQPOSScan();
  const {transactionInProgress, doTrade, cancelTrade} = useQPOSTrade();

  console.log('state', {
    error,
    devices,
    connected,
    scanning,
    transactionInProgress,
  });

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const onDevicePress = (device: QPOSScanDevice) => {
    connect(device.address);
  };
  const onPaymentPress = () => {
    doTrade({amount: '50'});
  };
  const onPaymentCancelPress = () => {
    cancelTrade();
  };

  const renderConnectedDevice = () => {
    return (
      <View>
        <Text>Connected</Text>
        <Button title="Payment" onPress={onPaymentPress} />
        {/* {transactionInProgress ? (
          <Button title="Cancel" onPress={onPaymentCancelPress} />
        ) : (
          <Button title="Payment" onPress={onPaymentPress} />
        )} */}
      </View>
    );
  };

  const renderDevices = () => {
    const renderItem = ({item}: {item: QPOSScanDevice}) => (
      <View style={styles.device}>
        <Button
          key={item.address}
          title={item.name}
          onPress={() => onDevicePress(item)}
        />
      </View>
    );

    return (
      <>
        <Text>Devices:</Text>
        <FlatList
          data={devices}
          refreshing={scanning}
          onRefresh={scan}
          keyExtractor={deviceKeyExtractor}
          renderItem={renderItem}
        />
      </>
    );
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <View style={styles.container}>
        {connected ? renderConnectedDevice() : renderDevices()}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    height: '100%',
    flexDirection: 'column',
    paddingHorizontal: 20,
    paddingTop: 20,
    backgroundColor: 'white',
  },
  device: {
    marginTop: 8,
  },
});

export default App;
