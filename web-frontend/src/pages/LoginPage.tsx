import React, { useState } from 'react';
import { Button, Card, Col, Form, Input, message, Modal, Row, Typography, theme } from 'antd';
import { useNavigate } from 'react-router-dom';
import { http } from '../api/http';
import { UserOutlined, LockOutlined, BookOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;
const { useToken } = theme;

type UserType = 'CUSTOMER' | 'ADMIN';

const LoginPage: React.FC = () => {
  const [userType, setUserType] = useState<UserType>('CUSTOMER');
  const [loading, setLoading] = useState(false);
  const [bookCount, setBookCount] = useState<number>(0);
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [registerForm] = Form.useForm();
  const [showRegisterModal, setShowRegisterModal] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);
  const { token } = useToken();

  React.useEffect(() => {
    const fetchCount = async () => {
      try {
        const resp = await http.get('/customer/books/count');
        setBookCount(resp.data);
      } catch (e) {
        console.error('Failed to fetch book count', e);
      }
    };
    fetchCount();
  }, []);

  const onFinish = async (values: { username: string; password: string }) => {
    try {
      setLoading(true);
      const resp = await http.post('/auth/login', {
        username: values.username.trim(),
        password: values.password,
        userType
      });
      const data = resp.data;
      message.success('登录成功，欢迎回来！');
      if (userType === 'CUSTOMER') {
        navigate('/customer', {
          state: {
            customerId: data.customerId,
            customerName: data.customerName
          }
        });
      } else {
        navigate('/admin', {
          state: {
            adminName: data.adminName
          }
        });
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="full-page-gradient" style={{ overflow: 'hidden' }}>
        <Row
          justify="center"
          align="middle"
          style={{ width: '100%', maxWidth: 1200, margin: '0 24px' }}
          gutter={[48, 24]}
        >
          {/* Left Branding Section */}
          <Col xs={0} md={12} lg={12} xl={12}>
            <div className="animate-fade-in" style={{ paddingRight: 48 }}>
              <div style={{ marginBottom: 24, display: 'flex', alignItems: 'center', gap: 12 }}>
                <div
                  className="animate-float animate-pulse-glow"
                  style={{
                    background: 'white',
                    borderRadius: 12,
                    width: 48,
                    height: 48,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                  }}
                >
                  <BookOutlined style={{ fontSize: 28, color: token.colorPrimary }} />
                </div>
                <Text strong style={{ fontSize: 24, color: '#fff', letterSpacing: 1 }}>BookStore</Text>
              </div>

              <Title level={1} style={{ color: '#fff', fontSize: 48, margin: '0 0 24px 0', lineHeight: 1.2 }}>
                探索<br />知识的海洋
              </Title>
              <Text style={{ color: 'rgba(255,255,255,0.85)', fontSize: 18, display: 'block', maxWidth: 480, lineHeight: 1.6 }}>
                网上书店管理系统提供一站式的图书浏览、购买与管理服务。
                连接读者与知识，让阅读更加触手可及。
              </Text>

              <div style={{ marginTop: 48, display: 'flex', gap: 16 }}>
                <div className="stat-card" style={{
                  background: 'rgba(255,255,255,0.1)',
                  backdropFilter: 'blur(10px)',
                  padding: '16px 24px',
                  borderRadius: 12,
                  border: '1px solid rgba(255,255,255,0.2)',
                  cursor: 'default'
                }}>
                  <Text style={{ color: '#fff', fontSize: 24, fontWeight: 'bold' }}>{bookCount > 0 ? bookCount : '...'}</Text>
                  <div style={{ color: 'rgba(255,255,255,0.6)', fontSize: 13 }}>在售图书</div>
                </div>
                <div className="stat-card" style={{
                  background: 'rgba(255,255,255,0.1)',
                  backdropFilter: 'blur(10px)',
                  padding: '16px 24px',
                  borderRadius: 12,
                  border: '1px solid rgba(255,255,255,0.2)',
                  cursor: 'default'
                }}>
                  <Text style={{ color: '#fff', fontSize: 24, fontWeight: 'bold' }}>24h</Text>
                  <div style={{ color: 'rgba(255,255,255,0.6)', fontSize: 13 }}>急速发货</div>
                </div>
              </div>
            </div>
          </Col>

          {/* Right Login Form */}
          <Col xs={24} md={12} lg={10} xl={8}>
            <Card
              className="glass-card animate-fade-in login-card-3d"
              bordered={false}
              style={{ padding: 24 }}
            >
              <div style={{ marginBottom: 32, textAlign: 'center' }}>
                <Title level={3} style={{ marginBottom: 8 }}>欢迎登录</Title>
                <Text type="secondary">请选择身份并输入账号密码</Text>
              </div>

              <Form
                layout="vertical"
                form={form}
                initialValues={{ userType: 'CUSTOMER' }}
                onFinish={onFinish}
                size="large"
                className="login-input-glow"
              >
                <Form.Item name="userType" style={{ marginBottom: 24 }}>
                  <div style={{
                    background: 'rgba(0,0,0,0.04)',
                    padding: 4,
                    borderRadius: 8,
                    display: 'flex',
                    position: 'relative'
                  }}>
                    {['CUSTOMER', 'ADMIN'].map((type) => (
                      <div
                        key={type}
                        onClick={() => setUserType(type as UserType)}
                        style={{
                          flex: 1,
                          textAlign: 'center',
                          padding: '8px 0',
                          cursor: 'pointer',
                          borderRadius: 6,
                          background: userType === type ? '#fff' : 'transparent',
                          boxShadow: userType === type ? '0 2px 8px rgba(0,0,0,0.1)' : 'none',
                          transition: 'all 0.3s ease',
                          fontWeight: 500,
                          color: userType === type ? token.colorPrimary : '#666'
                        }}
                      >
                        {type === 'CUSTOMER' ? '顾客' : '管理员'}
                      </div>
                    ))}
                  </div>
                </Form.Item>

                <Form.Item
                  name="username"
                  rules={[{ required: true, message: '请输入用户名' }]}
                >
                  <Input
                    prefix={<UserOutlined style={{ color: token.colorTextQuaternary }} />}
                    placeholder="用户名"
                    autoComplete="username"
                  />
                </Form.Item>

                <Form.Item
                  name="password"
                  rules={[{ required: true, message: '请输入密码' }]}
                >
                  <Input.Password
                    prefix={<LockOutlined style={{ color: token.colorTextQuaternary }} />}
                    placeholder="密码"
                    autoComplete="current-password"
                  />
                </Form.Item>

                <Form.Item style={{ marginBottom: 16 }}>
                  <Button
                    type="primary"
                    htmlType="submit"
                    block
                    loading={loading}
                    style={{
                      height: 48,
                      fontSize: 16,
                      fontWeight: 600,
                      background: `linear-gradient(135deg, ${token.colorPrimary}, ${token.colorInfo})`,
                      border: 'none'
                    }}
                  >
                    登 录
                  </Button>
                </Form.Item>

                {userType === 'CUSTOMER' && (
                  <div style={{ textAlign: 'center' }}>
                    <Button
                      type="link"
                      onClick={() => setShowRegisterModal(true)}
                    >
                      没有账号？立即注册 (顾客)
                    </Button>
                  </div>
                )}
              </Form>

              <div style={{ marginTop: 24, textAlign: 'center' }}>
                {/* Admin hint removed */}
              </div>
            </Card>
          </Col>
        </Row>
      </div>

      {/* Register Modal */}
      <Modal
        title="顾客注册"
        open={showRegisterModal}
        onCancel={() => {
          setShowRegisterModal(false);
          registerForm.resetFields();
        }}
        footer={null}
        destroyOnClose
        centered
      >
        <Form
          form={registerForm}
          layout="vertical"
          onFinish={async (values) => {
            try {
              setRegisterLoading(true);
              await http.post('/auth/register', {
                username: values.username.trim(),
                password: values.password,
                realName: values.realName?.trim() || values.username.trim(),
                phone: values.phone?.trim() || null,
                email: values.email?.trim() || null,
              });
              message.success('注册成功，请使用该账号登录');
              setShowRegisterModal(false);
              registerForm.resetFields();
            } catch (e: any) {
              message.error(e?.response?.data?.message || '注册失败');
            } finally {
              setRegisterLoading(false);
            }
          }}
        >
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认密码"
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="请再次输入密码" />
          </Form.Item>
          <Form.Item
            name="realName"
            label="真实姓名"
          >
            <Input placeholder="选填" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="手机号"
          >
            <Input placeholder="选填" />
          </Form.Item>
          <Form.Item
            name="email"
            label="邮箱"
          >
            <Input placeholder="选填" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" block loading={registerLoading}>
              注 册
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default LoginPage;


