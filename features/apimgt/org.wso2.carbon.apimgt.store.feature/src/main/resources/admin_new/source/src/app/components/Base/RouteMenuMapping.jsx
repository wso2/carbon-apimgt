import React from 'react';
import PeopleIcon from '@material-ui/icons/People';
import DnsRoundedIcon from '@material-ui/icons/DnsRounded';
import PermMediaOutlinedIcon from '@material-ui/icons/PhotoSizeSelectActual';
import PublicIcon from '@material-ui/icons/Public';
import SettingsEthernetIcon from '@material-ui/icons/SettingsEthernet';
import TimerIcon from '@material-ui/icons/Timer';
import SettingsIcon from '@material-ui/icons/Settings';
import PhonelinkSetupIcon from '@material-ui/icons/PhonelinkSetup';
import HomeIcon from '@material-ui/icons/Home';
import Dashboard from 'AppComponents/AdminPages/Dashboard/Dashboard';
import DemoTable from 'AppComponents/AdminPages/Microgateways/List';


const RouteMenuMapping = [
  {
    id: 'Admin Dashboard',
    icon: <HomeIcon />,
    path: '/dashboard',
    component: <Dashboard />,
    exact: true,
  },
  {
    id: 'Tasks',
    children: [
      { id: 'User Creation', path: '/tasks/user-creation', component: () => <DemoTable />, icon: <PeopleIcon /> },
      { id: 'Application Creation', path: '/tasks/application-creation', component: () => <DemoTable />, icon: <DnsRoundedIcon /> },
      { id: 'Subscription Creation', path: '/tasks/subscription-creation', component: () => <DemoTable />, icon: <PermMediaOutlinedIcon /> },
      { id: 'Application Registration', path: '/tasks/application-registration', component: () => <DemoTable />, icon: <PublicIcon /> },
      { id: 'API State Change', path: '/tasks/api-state-change', component: () => <DemoTable />, icon: <SettingsEthernetIcon /> },
    ],
  },
  { id: 'Microgateways', path: '/settings/mg-labels', component: () => <DemoTable />, icon: <PhonelinkSetupIcon /> },
  { id: 'API Categories', path: '/settings/api-categories', component: () => <DemoTable />, icon: <PhonelinkSetupIcon /> },
  { id: 'Bot Detection', path: '/settings/bot-detection', component: () => <DemoTable />, icon: <PhonelinkSetupIcon /> },
  {
    id: 'Settings',
    children: [
      { id: 'Applications', path: '/settings/applications', component: () => <DemoTable />, icon: <SettingsIcon /> },
      { id: 'Scope Mapping', path: '/settings/scope-mapping', component: () => <DemoTable />, icon: <TimerIcon /> },
      { id: 'Devportal Theme', path: '/settings/devportal-theme', component: () => <DemoTable />, icon: <PhonelinkSetupIcon /> },
    ],
  },
  {
    id: 'Throttling Policies',
    children: [
      { id: 'Advanced Throttling Policies', path: '/throttling/advanced', component: () => <DemoTable />, icon: <SettingsIcon /> },
      { id: 'Application Throttling Policies', path: '/throttling/application', component: () => <DemoTable />, icon: <TimerIcon /> },
      { id: 'Subscription Throttling Policies', path: '/throttling/subscription', component: () => <DemoTable />, icon: <PhonelinkSetupIcon /> },
      { id: 'Custom Throttling Policies', path: '/throttling/custom', component: () => <DemoTable />, icon: <PhonelinkSetupIcon /> },
      { id: 'Blacklisted Items', path: '/throttling/blacklisted', component: () => <DemoTable />, icon: <PhonelinkSetupIcon /> },
    ],
  },

];

export default RouteMenuMapping;