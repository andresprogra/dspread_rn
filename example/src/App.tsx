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

import { Colors } from 'react-native/Libraries/NewAppScreen';
import useQPOS from './hooks/useQPOS';
import useQPOSScan from './hooks/useQPOSScan';
import useQPOSTrade from './hooks/useQPOSTrade';
import type { QPOSScanDevice } from 'react-native-dspread';

const deviceKeyExtractor = (device: QPOSScanDevice) => device.address;

const App = () => {
  const isDarkMode = useColorScheme() === 'dark';
  const { qpos, error, connected, connecting, connect } = useQPOS();
  const { scanning, devices, scan } = useQPOSScan();
  const { processing, messages, doTrade } = useQPOSTrade();

  console.log(
    'state',
    JSON.stringify(
      {
        error,
        qpos: {
          connecting,
          connected,
          ...qpos,
        },
        scan: {
          devices,
          scanning,
        },
        trade: {
          processing,
          messages,
        },
      },
      null,
      2
    )
  );

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const onDevicePress = (device: QPOSScanDevice) => {
    connect(device.address);
  };
  const onPaymentPress = () => {
    doTrade({ amount: '50' });
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
        <ScrollView>
          <Text>{messages.join('\n')}</Text>
        </ScrollView>
      </View>
    );
  };

  const renderDevices = () => {
    const renderItem = ({ item }: { item: QPOSScanDevice }) => (
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
        <Button title="Scan" onPress={scan}  />
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
