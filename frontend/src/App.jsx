import { Navigate, Route, Routes } from 'react-router-dom'; import { useAuth, HOME } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute'; import { Spinner } from './components/States';
import Landing from './pages/Landing'; import Login from './pages/Login'; import Register from './pages/Register';
import StudentDashboard from './pages/StudentDashboard'; import RecruiterDashboard from './pages/RecruiterDashboard'; import AgencyDashboard from './pages/AgencyDashboard';
import Simulator from './pages/Simulator'; import StudentWhatIf from './pages/StudentWhatIf'; import Assessment from './pages/Assessment'; import Alerts from './pages/Alerts'; import Outcomes from './pages/Outcomes';
function Home() { const { user, loading } = useAuth(); if (loading) return <Spinner/>; return user ? <Navigate to={HOME[user.role]} replace/> : <Landing/>; }
const P = ({ role, children }) => <ProtectedRoute role={role}>{children}</ProtectedRoute>;
export default function App() {
  return (<Routes><Route path="/" element={<Home/>}/><Route path="/login" element={<Login/>}/><Route path="/register" element={<Register/>}/>
    <Route path="/student" element={<P role="STUDENT"><StudentDashboard/></P>}/><Route path="/whatif" element={<P role="STUDENT"><StudentWhatIf/></P>}/>
    <Route path="/assessment" element={<P role="STUDENT"><Assessment/></P>}/>
    <Route path="/recruiter" element={<P role="RECRUITER"><RecruiterDashboard/></P>}/><Route path="/agency" element={<P role="COURSE_AGENCY"><AgencyDashboard/></P>}/>
    <Route path="/simulator" element={<P><Simulator/></P>}/><Route path="/alerts" element={<P><Alerts/></P>}/><Route path="/outcomes" element={<P><Outcomes/></P>}/>
    <Route path="*" element={<Navigate to="/" replace/>}/></Routes>);
}
